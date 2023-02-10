# -*- coding: utf8 -*-
import os
from qcloud_cos import CosConfig
from qcloud_cos import CosS3Client
from PIL import Image

SECRET_ID = os.environ.get('COS_SECRET_ID')
SECRET_KEY = os.environ.get('SECRET_KEY')
ARONA_BACKEND_ADMIN_SECRET_KEY = os.environ.get('ARONA_BACKEND_ADMIN_SECRET_KEY')
COS_ID = os.environ.get('COS_ID')
COS_NAME = os.environ.get('COS_NAME')
REGION = os.environ.get('REGION')
LOCAL_TMP_PATH = os.environ.get('LOCAL_TMP_PATH') # etc /tmp
BASE_FOLDER = os.environ.get('BASE_FOLDER') # etc image
RAW_FOLDER = BASE_FOLDER + os.environ.get('RAW_FOLDER') # etc image/raw
PNG_FOLDER = BASE_FOLDER + os.environ.get('PNG_FOLDER') # etc image/png
WEBP_FOLDER = BASE_FOLDER + os.environ.get('WEBP_FOLDER') # etc image/webp
BUCKET = COS_NAME + "-" + COS_ID
config = CosConfig(Region=REGION, SecretId=SECRET_ID, SecretKey=SECRET_KEY)
client = CosS3Client(config)

def compress_to_webp(source: str, dest: str):
    im = Image.open(source)
    im.save(dest, format="webp", quality=95, method=6)

def compress_to_png(source: str, dest: str):
    im = Image.open(source)
    im.save(dest)
    while os.path.getsize(dest) / 1024 / 1024 > 5.5:
        im = Image.open(dest)
        (x, y) = im.size
        resize = im.resize((int(x * 0.95), int(y * 0.95)), Image.ANTIALIAS)
        resize.save(dest)

def download_from_cos(source: str, dest: str):
    # 下载图片
    response = client.download_file(
        Bucket=BUCKET, 
        Key=source,
        DestFilePath=dest
    )
    print(response)

def upload_to_cos(source: str, dest: str):
    # 上传处理好的文件
    response = client.upload_file(
        Bucket=BUCKET, 
        Key=dest,
        LocalFilePath=source
    )
    print(response)

def delete_from_local(source):
    if os.path.isfile(source):
        try:
            os.remove(source)
        except:
            pass
    elif os.path.isdir(source):
        for item in os.listdir(source):
            itemsrc = os.path.join(source, item)
            delete_from_local(itemsrc)
        try:
            os.rmdir(source)
        except:
            pass

def notify_admin(source):
    pass

def flush_cdn(source):
    pass

def delete_object(source):
    response = client.delete_object(
        Bucket=BUCKET,
        Key=source
    )
    print(response)

def main_handler(event, context):
    for record in event["Records"]:
        event_name = record["event"]["eventName"]
        if event_name != "cos:ObjectCreated:*":
            pass
        # /${COS_ID}${RAW_FOLDER}/... etc /1234312/image/raw/some/test.png
        key = str(record["cos"]["cosObject"]["key"])
        # etc /some/test.png
        file_absolute_path = key[key.rfind(RAW_FOLDER) + len(RAW_FOLDER):]
        # etc image/raw/some/test.png
        file_absolute_path_no_id = key.replace("/%s/" % COS_ID, "")
        # etc test.png
        file_name = file_absolute_path[key.rfind("/") + 1:]
        # 数据获取完成

        # 将cos图片下载到本地目录
        file_tmp_local_path = LOCAL_TMP_PATH + "/" + file_name # /tmp/test.png
        download_from_cos(file_absolute_path_no_id, file_tmp_local_path)

        # 进行png压缩
        file_tmp_local_path_png = LOCAL_TMP_PATH + "/" + "tmp_png.png"  # /tmp/tmp_png.png
        compress_to_png(file_tmp_local_path, file_tmp_local_path_png)

        # 进行webp压缩
        file_tmp_local_path_webp = LOCAL_TMP_PATH + "/" + "tmp_webp.webp"  # /tmp/tmp_webp.webp
        compress_to_webp(file_tmp_local_path, file_tmp_local_path_webp)

        # 上传压缩好的文件
        # 上传png
        cos_png = PNG_FOLDER + file_absolute_path
        upload_to_cos(file_tmp_local_path_png, cos_png)
        # 上传webp
        cos_webp = WEBP_FOLDER + file_absolute_path
        upload_to_cos(file_tmp_local_path_webp, cos_webp)

        # 删除本地文件
        delete_from_local(file_tmp_local_path)
        delete_from_local(file_tmp_local_path_png)
        delete_from_local(file_tmp_local_path_webp)
        # todo 通知admin

        # notify_admin()

        # todo 通知cdn

        # flush_cdn()

        # exit
    return("Success")
