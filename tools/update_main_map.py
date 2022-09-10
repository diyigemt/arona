import sqlite3
import os
import hashlib
da_path = r"C:\Users\qwe13\Desktop\data.db"
img_folder = "image/chapter_map/"

if __name__ == '__main__':
    connection = sqlite3.connect(da_path)
    cursor = connection.cursor()
    index = 0
    print("数据库连接成功")
    for file in os.listdir(img_folder):
        file_name = file.replace(".png", "")
        file_path = img_folder + file
        # 计算md5
        hash = ""
        with open(file_path, "rb") as f:
            hash = hashlib.md5(f.read()).digest().hex()
        cursor.execute("SELECT * FROM `image` WHERE `hash` = '%s' OR `name` = '%s'" % (hash, file_name))
        history = cursor.fetchone()
        # 有记录 更新path和hash
        if history != None:
            cursor.execute("UPDATE `image` SET `path` = '%s', `hash` = '%s'" % (file_path, hash))
        else:
        # 否则新建记录   
            cursor.execute("INSERT INTO `image`(`name`, `path`, `hash`) VALUES ('%s', '%s', '%s')" % (file_name, file_path, hash))
        connection.commit()
        index += 1
    print("成功! %d" % index)