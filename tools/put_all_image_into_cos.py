import os
import math
import getpass
import json
import codecs
import shutil
import posixpath
from datetime import datetime
from qcloud_cos import CosConfig
from qcloud_cos import CosS3Client
from qcloud_cos.cos_threadpool import SimpleThreadPool

from reflash_cdn import purgeFiles
from tools import confirm_action, post_data, safe_move, update_image_from_api
from progressbar import ProgressBar, Percentage, Bar, Timer, ETA
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import Tuple, Union
COS_ID = ""
COS_NAME = ""
SECRET_ID = ""
SECRET_KEY = ""
Bucket = ""
REGION = "ap-shanghai"
BASE_FOLDER = "image"
# 备份文件夹: 上传前先把 COS 上即将被覆盖的旧对象拷贝到这里, 与 image 平级
BACKUP_FOLDER = "backup"

CHAPTER_MAP_PATH = "chapter_map"
SOME_PATH = "some"
STUDENT_PATH = "student_rank"
CHAPTER_MAP_S_PATH = "s/chapter_map"
SOME_S_PATH = "s/some"
STUDENT_S_PATH = "s/student_rank"

def list_folder(folder: str) -> list[Tuple[str, str]]:
    list_name = []
    local_base_path = os.path.join(BASE_FOLDER, folder)
    for file in os.listdir(local_base_path):
        file_no_extend_name = file[:file.rfind(".")]
        full_path = os.path.join(local_base_path, file).replace("\\", "/")
        list_name.append((file_no_extend_name, full_path))
    return list_name

def build_backup_key(key: str, backup_date: str) -> str:
    """生成 COS 上的备份对象 key。

    备份统一放到 BACKUP_FOLDER 文件夹下, 保留相对 BASE_FOLDER 的子目录结构,
    并在文件名后追加备份日期, 形如:
        image/some/foo.png      -> backup/some/foo_20260615.png
    """
    normalized = key.replace("\\", "/")
    base_prefix = BASE_FOLDER + "/"
    relative_key = normalized[len(base_prefix):] if normalized.startswith(base_prefix) else normalized
    dir_name = posixpath.dirname(relative_key)
    name, ext = posixpath.splitext(posixpath.basename(relative_key))
    backup_name = f"{name}_{backup_date}{ext}"
    return posixpath.join(BACKUP_FOLDER, dir_name, backup_name)


def do_upload(client: CosS3Client, Bucket: str, path: str, backup_date: str) -> Tuple[Union[Exception, None], str]:
    key = path.replace("\\", "/")
    try:
        # 上传前先把 COS 上即将被覆盖的同名对象拷贝到备份文件夹, 以保留旧版本。
        # s 系列(image/s/ 前缀)无需备份, 它可由对应原图的备份重新生成。
        # object_exists 在鉴权/网络等非 404 错误时会抛异常, 此时一并视为上传失败,
        # 避免在备份缺失的情况下直接覆盖原文件。
        is_s_key = key.startswith(f"{BASE_FOLDER}/s/")
        if not is_s_key and client.object_exists(Bucket, key):
            backup_key = build_backup_key(key, backup_date)
            # 当天已存在备份则保留首次备份, 不再覆盖。
            if not client.object_exists(Bucket, backup_key):
                client.copy_object(
                    Bucket=Bucket,
                    Key=backup_key,
                    CopySource={"Bucket": Bucket, "Key": key, "Region": REGION},
                )
        client.upload_file(Bucket, key, path)
    except Exception as e:
        return (e, path)
    return (None, path)


if __name__ == "__main__":
    if os.path.exists(r"C:\Users\%s\.ssh\arona-cos.json" % getpass.getuser()):
        with codecs.open(r"C:\Users\%s\.ssh\arona-cos.json" % getpass.getuser(), "r", encoding="utf-8") as f:
            read = json.loads(f.read())
            COS_ID = read["COS_ID"]
            COS_NAME = read["COS_NAME"]
            SECRET_ID = read["SECRET_RAW_ID"]
            SECRET_KEY = read["SECRET_RAW_KEY"]
            Bucket = COS_NAME + "-" + COS_ID
    config = CosConfig(Region=REGION, SecretId=SECRET_ID, SecretKey=SECRET_KEY)
    client = CosS3Client(config)
    pool = SimpleThreadPool()
    # 遍历some文件夹
    some_list = list_folder(SOME_PATH)
    # 遍历student_rank文件夹
    student_list = list_folder(STUDENT_PATH)
    # 遍历chapter_map文件夹
    chapter_list = list_folder(CHAPTER_MAP_PATH)
    if len(some_list) > 0:
        print("some: %s" % ", ".join(list(map(lambda tu: tu[0], some_list))))
    if len(student_list) > 0:
        print("student: %s" % ", ".join(list(map(lambda tu: tu[0], student_list))))
    if len(chapter_list) > 0:
        print("chapter: %s" % ", ".join(list(map(lambda tu: tu[0], chapter_list))))
    if not confirm_action():
        exit(0)
    student_data = update_image_from_api(STUDENT_PATH, type=1) if len(student_list) > 0 else []
    chapter_map_data = update_image_from_api(CHAPTER_MAP_PATH, type=2) if len(chapter_list) > 0 else []
    some_data = update_image_from_api(SOME_PATH, type=3) if len(some_list) > 0 else []
    _upload_file_list = some_list + student_list + chapter_list
    if len(_upload_file_list) <= 0:
        print("empty")
        exit(0)
    # 频道用的s系列
    upload_file_s_list = list_folder(SOME_S_PATH) + list_folder(STUDENT_S_PATH) + list_folder(CHAPTER_MAP_S_PATH)
    # 过滤只用于上传的
    upload_file_s_list = list(filter(lambda x: len([it for it in _upload_file_list if it[0] == x[0]]) > 0, upload_file_s_list))
    upload_file_list = _upload_file_list + upload_file_s_list
    backend_data_list = student_data + chapter_map_data + some_data
    widgets = ["Progress: ", Percentage(), " ", Bar("#"), " ",
               Timer(), " ", ETA()]
    pbar = ProgressBar(widgets=widgets, maxval=100).start()
    finish = 0
    error_list: list[Tuple[Exception, str]] = []
    # 整批共用同一个备份日期: 当天已存在备份时不再覆盖, 以保留当天首次备份。
    backup_date = datetime.now().strftime("%Y%m%d")
    with ThreadPoolExecutor(max_workers=8) as executor:
        futures = [executor.submit(
            do_upload, client, Bucket, it[1], backup_date) for it in upload_file_list]
        for future in as_completed(futures):
            resp = future.result()  # 上传完成，可做后续处理
            if resp[0] != None:
                error_list.append(resp)
            else:
                local_path = resp[1]
                # 移动到history文件夹
                # s系列不管
                if local_path.find("/s/") == -1:
                    file_history_path = str(local_path).replace("image", "image/history")
                    safe_move(local_path, file_history_path)
            finish = finish + 1
            pbar.update(math.ceil(finish / len(upload_file_list) * 100))
    pbar.finish()
    backend_data = []
    for item in _upload_file_list:
        has_error = [it for it in error_list if it[1] == item[1]]
        if has_error:
            print(f"file: {item[1]} upload failed: {has_error[0][0]}")
            continue
        b_data = [it for it in backend_data_list if item[1].find(it["name"]) != -1]
        if b_data:
            backend_data.append(b_data[0])
    print(backend_data)
    post_data("imageUpdate", backend_data)