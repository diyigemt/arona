import sqlite3


if __name__ == '__main__':
    connection = sqlite3.connect("./data.db")
    cursor = connection.cursor()
    print("数据库连接成功")
    cursor.execute("SELECT `id` FROM `image`")
    for id in cursor.fetchall():
        cursor.execute("SELECT * FROM `image` WHERE `id` = '%s'" % id[0])
        row = cursor.fetchone()
        path = str(row[2])
        if len(path) < 10:
            cursor.execute("SELECT * FROM `image` WHERE `id` = '%s'" % path)
            father = cursor.fetchone()
            father_path = father[2]
            father_hash = father[3]
            cursor.execute("UPDATE `image`(`path`, `hash`) VALUES ('%s', '%s')" % (father_path, father_hash))
            connection.commit()