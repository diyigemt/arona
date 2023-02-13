import os
import getpass
import json
import codecs
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

# folder etc /some
def list_folder(folder: str):
    list_name = []
    local_base_path = "./image%s" % folder
    for file in os.listdir(local_base_path):
        file_no_extend_name = file[:file.rfind(".")]
        full_path = local_base_path + "/" + file
        list_name.append((file_no_extend_name, BASE_FOLDER + folder + "/" + file, full_path))
    return list_name

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
    # 遍历some文件夹
    some_list = list_folder("/some")
    # 遍历student_rank文件夹
    student_list = list_folder("/student_rank")
    # 遍历chapter_map文件夹
    chapter_list = list_folder("/chapter_map")
    print("some: %s" %",".join(list(map(lambda tu: tu[0], some_list))))
    print("student: %s" % ",".join(list(map(lambda tu: tu[0], student_list))))
    print("chapter: %s" % ",".join(list(map(lambda tu: tu[0], chapter_list))))
    confirm_action()
    for item in some_list + student_list + chapter_list:
        pool.add_task(client.upload_file, Bucket, item[1], item[2])
    pool.wait_completion()
    pass