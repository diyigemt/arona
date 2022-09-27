import sqlite3
from tools import update_image, update_image_from_api


da_path = r"C:\Users\hgny\Desktop\data.db"
img_folder = "image"
base_folder = "/some/"


# 更新杂图

if __name__ == '__main__':
    connection = sqlite3.connect(da_path)
    cursor = connection.cursor()
    print("数据库连接成功")
    index = update_image(base_folder, cursor, connection, type=3)
    print("成功! %d" % index)
    # update_image_from_api(base_folder, type=3)
