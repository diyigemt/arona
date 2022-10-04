import sqlite3
from tools import update_image, update_image_from_api
da_path = r"C:\Users\qwe13\Desktop\data.db"
img_folder = "image"
base_folder = "/chapter_map/"

# 更新main_map图片文件夹下的图片

if __name__ == '__main__':
    # connection = sqlite3.connect(da_path)
    # cursor = connection.cursor()
    # index = update_image(base_folder, cursor, connection, type=2)
    # print("成功! %d" % index)
    update_image_from_api(base_folder, type=2)