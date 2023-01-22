import sqlite3
import datetime

start_date = datetime.datetime.strptime("2022-11-25", "%Y-%m-%d")
end_date = datetime.datetime.strptime("2023-01-21", "%Y-%m-%d")
lens = 56

if __name__ == "__main__":
    conn = sqlite3.connect("./NLPH.db")
    count = {}
    for index in range(lens):
        next_date = start_date + datetime.timedelta(days=1)
        current_timestamp = str(start_date.timestamp())[:10] + "000"
        next_timestamp = str(next_date.timestamp())[:10] + "000"
        start_date = start_date + datetime.timedelta(days=1)
        sql = "SELECT DISTINCT `sender` FROM `NLPH` WHERE `date` >= '%s' AND  `date` < '%s'" % (current_timestamp, next_timestamp)
        for item in conn.execute(sql):
            sender = item[0]
            if sender not in count:
                count[sender] = 1
            else:
                count[sender] = count[sender] + 1
    sender_list = list(count.keys())
    sender_list.sort(key=lambda sender: count[sender])
    sender_list = list(filter(lambda sender: count[sender] > 40, sender_list))
    print(sender_list)
    print(len(sender_list))
    