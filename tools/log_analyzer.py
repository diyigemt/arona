import os
import codecs
import sqlite3


if __name__ == "__main__":
    conn = sqlite3.connect("log.db")
    cursor = conn.cursor()
    init_sql = """
CREATE TABLE IF NOT EXISTS "Summary" (
  "id" INTEGER PRIMARY KEY AUTOINCREMENT,
  "key" VARCHAR(50) NOT NULL,
  "time" VARCHAR(50) NOT NULL
);
"""
    cursor.execute(init_sql)
    conn.commit()
    arona_count = 0
    other_count = 0
    files = os.listdir("./log")
    for index, file in enumerate(files):
        file_path = "./log/" + file
        with codecs.open(file_path, "r", encoding="utf-8") as f:
            lines = f.readlines()
            for line in lines:
                if line.find('GET: /api/v1/image with {"name":"') == -1:
                    continue
                splits = line.split(" ")
                time = splits[0]
                date = time[1:11]
                date_time = time[12:-5]
                level = splits[1]
                time = "%s %s" % (date, date_time)
                if level == "[INFO]":
                    key = splits[7]
                    arona_count = arona_count + 1
                else:
                    key = splits[9]
                    other_count = other_count + 1
                key = key[9:-2]
                try:
                    sql = "INSERT INTO `Summary`(`key`,`time`) VALUES ('%s','%s')" % (key, time)
                    cursor.execute(sql)
                    conn.commit()
                except Exception:
                    pass
        print("analysis file: %s success, remain: %d/%d" % (file, index + 1, len(files)))
    print("analysis success")
    print("from arona: %d" % arona_count)
    print("from other: %d" % other_count)
                