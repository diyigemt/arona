import sqlite3
import os
file_path = r"C:\Users\qwe13\Desktop\data.db"
name_and_code = r".\image\student_rank"
def replace0(source):
    first = source.find("(")
    last = source.find(")")
    if first != -1:
        source = source[first+1: last] + source[0:first]
    return source
if __name__ == '__main__':
    connection = sqlite3.connect(file_path)
    cursor = connection.cursor()
    index = 0
    print("数据库连接成功")
    for file in os.listdir(name_and_code):
        if file.find("黑兔") != -1:
            a = 1
            pass
        divs = file.replace(".png", "")
        names = divs.split("_")
        first_name = names[0]
        last_name = names[1]
        last_name = replace0(last_name)
        cursor.execute("INSERT INTO `student_rank_0`(`name`, `code`) VALUES ('%s', '%s')" % (last_name, file))
        connection.commit()
        if len(names) > 2:
            alias = names[2: len(names)]
            cursor.execute("SELECT * FROM `student_rank_0` WHERE `code` = '%s'" % file)
            id = cursor.fetchone()[0]
            for a in alias:
                a = replace0(a)
                cursor.execute("INSERT INTO `student_rank_0`(`name`, `code`) VALUES ('%s', '%s')" % (a, id))
                connection.commit() 
        index += 1
    print("成功! %d" % index)