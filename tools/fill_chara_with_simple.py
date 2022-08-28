import sqlite3
import os
from os import system
from unicodedata import name
file_path = r"F:\code\node\arona-backend\data\data.db"
name_and_code = r"G:\verysync\arona\student_insert"
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
    with open(name_and_code, encoding="utf8") as f:
        for line in f.readlines():
            divs = line.split(" ")
            names = divs[0].split("_")
            first_name = names[0]
            last_name = names[1]
            last_name = replace0(last_name)
            code = divs[1]
            cursor.execute("INSERT INTO `student_rank`(`name`, `code`) VALUES ('%s', '%s')" % (last_name, code))
            connection.commit()
            if len(names) > 2:
                alias = names[2: len(names)]
                cursor.execute("SELECT * FROM `student_rank` WHERE `code` = '%s'" % code)
                id = cursor.fetchone()[0]
                for a in alias:
                    a = replace0(a)
                    cursor.execute("INSERT INTO `student_rank`(`name`, `code`) VALUES ('%s', '%s')" % (a, id))
                    connection.commit() 
            index += 1
    print("成功! %d" % index)