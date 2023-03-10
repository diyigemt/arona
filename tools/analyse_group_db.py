import sqlite3
import datetime
import codecs
import json
start_date = datetime.datetime.strptime("2023-03-07 09:57:25", "%Y-%m-%d %H:%M:%S")
end_date = datetime.datetime.strptime("2023-03-07 11:18:00", "%Y-%m-%d %H:%M:%S")

if __name__ == "__main__":
    conn = sqlite3.connect("./NLPH.db")
    cursor = conn.cursor()
    with codecs.open("./726453107.txt", "r", encoding="utf-8") as f:
        lines = f.readlines()
        name_map = {}
        for it in lines:
            d = it.split(" ")
            permission = d[0]
            id = d[1]
            remark = d[2].replace("\n", "")
            name_map[id] = {
                "permission": permission,
                "remark": remark
            }
    start_timestamp = str(start_date.timestamp())[:10] + "000"
    end_timestamp = str(end_date.timestamp())[:10] + "000"
    sql = "SELECT * FROM `NLPH` WHERE `date` >= '1678118400000'"
    cursor.execute(sql)
    record = {}
    record_list = []
    for item in cursor.fetchall():
        sender = str(item[0])
        content = str(item[3])
        try:
            remark = name_map[sender]["remark"]
        except Exception as _:
            remark = "查无此人"
        record_list.append({
            "remark": remark,
            "content": content
        })
    record["records"] = record_list
    with codecs.open("record.json", "w", encoding="utf-8") as f:
        f.write(json.dumps(record, ensure_ascii=False))
        f.flush()
    cursor.close()
    conn.close()