import os
import getpass
import json
import codecs
import shutil
from qcloud_cos import CosConfig
from qcloud_cos import CosS3Client
from qcloud_cos.cos_threadpool import SimpleThreadPool

from tools import confirm_action

COS_ID = ""
COS_NAME = ""
SECRET_ID = ""
SECRET_KEY = ""
Bucket = ""
REGION = "ap-shanghai"
BASE_FOLDER = "image"

if __name__ == "__main__":
    if os.path.exists(r"C:\Users\%s\.ssh\arona-cos.json" % getpass.getuser()):
        with codecs.open(r"C:\Users\%s\.ssh\arona-cos.json" % getpass.getuser(), "r", encoding="utf-8") as f:
            read = json.loads(f.read())
            COS_ID = read["COS_ID"]
            COS_NAME = read["COS_NAME"]
            SECRET_ID = read["SECRET_ID"]
            SECRET_KEY = read["SECRET_KEY"]
            Bucket = COS_NAME + "-" + COS_ID 
    config = CosConfig(Region=REGION, SecretId=SECRET_ID, SecretKey=SECRET_KEY)
    client = CosS3Client(config)
    pool = SimpleThreadPool()
    action_list = [] # file_name local_path cos_path
    for root, dirs, files in os.walk("./raw"):
        if root.startswith("./raw\\_history"):
            continue
        base_path = root.replace("\\", "/").replace("./raw", "file")
        for file in files:
            action_list.append((file, root.replace("\\", "/") + "/" + file, base_path + "/" + file))
    print("list: %s" % ", ".join(list(map(lambda tu: tu[0], action_list))))
    confirm_action()
    for item in action_list:
        pool.add_task(client.upload_file, Bucket, item[2], item[1])
    pool.wait_completion()
    # 移动到history文件夹
    for item in action_list:
        local_path = item[1]
        file_history_path = str(local_path).replace("raw", "raw/_history")
        parent_path = file_history_path[:file_history_path.rfind("/")]
        if not os.path.exists(parent_path):
            os.makedirs(file_history_path[:file_history_path.rfind("/")])
        shutil.move(local_path, file_history_path)
    pass