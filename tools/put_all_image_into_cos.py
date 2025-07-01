import os
import math
import getpass
import json
import codecs
import shutil
from qcloud_cos import CosConfig
from qcloud_cos import CosS3Client
from qcloud_cos.cos_threadpool import SimpleThreadPool

from reflash_cdn import purgeFiles
from tools import confirm_action, post_data, update_image_from_api
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

CHAPTER_MAP_PATH = "chapter_map"
SOME_PATH = "some"
STUDENT_PATH = "student_rank"

# folder etc /some


def list_folder(folder: str) -> list[Tuple[str, str]]:
    list_name = []
    local_base_path = os.path.join(BASE_FOLDER, folder)
    for file in os.listdir(local_base_path):
        file_no_extend_name = file[:file.rfind(".")]
        full_path = os.path.join(local_base_path, file).replace("\\", "/")
        list_name.append((file_no_extend_name, full_path))
    return list_name

def do_upload(client: CosS3Client, Bucket: str, path: str) -> Union[Tuple[Exception, str], None]:
    try:
        client.upload_file(Bucket, path, path)
    except Exception as e:
        return (e, path)
    return None


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
    print("some: %s" % ", ".join(list(map(lambda tu: tu[0], some_list))))
    print("student: %s" % ", ".join(list(map(lambda tu: tu[0], student_list))))
    print("chapter: %s" % ", ".join(list(map(lambda tu: tu[0], chapter_list))))
    student_data = update_image_from_api(STUDENT_PATH, type=1)
    chapter_map_data = update_image_from_api(CHAPTER_MAP_PATH, type=2)
    some_data = update_image_from_api(SOME_PATH, type=3)
    upload_file_list = some_list + student_list + chapter_list
    backend_data_list = student_data + chapter_map_data + some_data
    widgets = ["Progress: ", Percentage(), " ", Bar("#"), " ",
               Timer(), " ", ETA()]
    pbar = ProgressBar(widgets=widgets, maxval=len(upload_file_list)).start()
    pbar.update(1)
    pbar.finish()
    finish = 0
    error_list: list[Tuple[Exception, str]] = []
    with ThreadPoolExecutor(max_workers=4) as executor:
        futures = [executor.submit(
            do_upload, client, Bucket, it[1]) for it in upload_file_list]
        for future in as_completed(futures):
            resp = future.result()  # 上传完成，可做后续处理
            if resp:
                error_list.append(resp)
            finish = finish + 1
            pbar.update(math.ceil(finish / len(upload_file_list)))
    backend_data = []
    for item in upload_file_list:
        has_error = [it for it in error_list if it[1] == item[1]]
        if has_error:
            print(f"file: {has_error[1]} upload failed.")
            continue
        local_path = item[1]
        # 移动到history文件夹
        file_history_path = str(local_path).replace("image", "image/history")
        shutil.move(local_path, file_history_path)
        b_data = [it for it in backend_data_list if item[1].find(it["name"]) != -1]
        if b_data:
            backend_data.append(b_data[0])
    print(backend_data)
    # post_data("imageUpdate", backend_data)
