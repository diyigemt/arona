import sqlite3
import os
import hashlib
da_path = r"C:\Users\qwe13\Desktop\data.db"

## 批量更新数据库
if __name__ == '__main__':
    connection = sqlite3.connect(da_path)
    cursor = connection.cursor()
    index = 0
    print("数据库连接成功")
    cursor.execute("SELECT * FROM `image`")
    for item in cursor.fetchall():
        id = str(item[0])
        path = str(item[2])
        if path.find("image") != -1:
            path = path.replace("image/", "")
            cursor.execute("UPDATE `image` SET `path` = '%s' WHERE `id` = '%s'" % (path, id))
            connection.commit()
            index += 1
    print("成功! %d" % index)