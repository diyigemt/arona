import math
import os
import codecs
import sqlite3
import threading
max_thread = min(8, os.cpu_count())

def split_arr(arr, size):
    size = math.ceil(len(arr) / size)
    s = []
    for i in range(0, int(len(arr)) + 1, size):
        c = arr[i:i + size]
        if c != []:
            s.append(c)
    return s
ignore_prefix = ["@", "/", "#", "$", "%", "&", "*", "(", "（", ")", "）", "+", "-", ".", "!", "！", "，", "[", ",", "的"]
def analysis(files: list[str], index: int):
    conn = sqlite3.connect("./db/" + str(index) + ".db")
    cursor = conn.cursor()
    drop_sql = 'DROP TABLE IF EXISTS "Summary"'
    init_sql = """
CREATE TABLE IF NOT EXISTS "Summary" (
  "id" INTEGER PRIMARY KEY AUTOINCREMENT,
  "user" VARCHAR(50) NOT NULL,
  "key" VARCHAR(50) NOT NULL,
  "time" VARCHAR(50) NOT NULL
);
"""
    cursor.execute(drop_sql)
    cursor.execute(init_sql)
    conn.commit()
    for index, file in enumerate(files):
        file_path = "./log/" + file
        with codecs.open(file_path, "r", encoding="utf-8") as f:
            lines = f.readlines()
            for line in lines:
                if line.find('GET: /api/v1/image with {"name":"') == -1:
                    continue
                splits = line.split(" ")
                level = splits[1]
                if len(splits) < 12:
                    continue
                if level == "[INFO]":
                    key = splits[7]
                    user = splits[9]
                else:
                    key = splits[9]
                    user = splits[11]
                key = key[9:-2]
                try:
                    if key[0] in ignore_prefix:
                        continue
                except Exception:
                    continue
                time = splits[0]
                date = time[1:11]
                date_time = time[12:-5]
                time = "%s %s" % (date, date_time)
                try:
                    sql = "INSERT INTO `Summary`(`user`,`key`,`time`) VALUES ('%s','%s','%s')" % (user, key, time)
                    cursor.execute(sql)
                except Exception:
                    pass
        conn.commit()
        print("analysis file: %s success, remain: %d/%d" % (file, index + 1, len(files)))
    cursor.close()
    conn.close()
def join_table():
    conn = sqlite3.connect("./db/0.db")
    cursor = conn.cursor()
    for index in range(1, max_thread):
        cursor.execute(f"ATTACH DATABASE './db/{index}.db' AS db{index}")
        cursor.execute(f"INSERT INTO main.Summary(`user`,`key`,`time`) SELECT `user`,`key`,`time` FROM db{index}.Summary")
    conn.commit()
    cursor.close()
    conn.close()

if __name__ == "__main__":
    target = os.listdir("./log")
    splited_arr = split_arr(target, max_thread)
    threads = [threading.Thread(target=analysis, args=(arr,index,)) for index, arr in enumerate(splited_arr)]
    print("start with %d threads" % len(threads))
    for t in threads:
        t.start()
    for t in threads:
        t.join()
    join_table()