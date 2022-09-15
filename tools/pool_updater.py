import os
import json
import dotmap
import sqlite3
sql_select_from_chara_by_name = "SELECT * FROM `GachaCharacters` WHERE `name` = '%s'"
sql_select_from_chara_by_id = "SELECT * FROM `GachaCharacters` WHERE `id` = '%s'"
sql_insert_into_chara = "INSERT INTO `GachaCharacters` (`name`, `star`, `limit`) VALUES ('%s', '%s', '%s')"
sql_select_from_pool_by_name = "SELECT * FROM `GachaPools` WHERE `name` = '%s'"
sql_insert_into_pool = "INSERT INTO `GachaPools` (`name`) VALUES ('%s')"
sql_select_from_pool_student_by_id = "SELECT * FROM `GachaPoolCharacters` WHERE `pool_id` = '%s' AND `character_id` = '%s'"
sql_insert_into_pool_student = "INSERT INTO `GachaPoolCharacters` (`pool_id`, `character_id`) VALUES ('%s', '%s')"

def insertStudent(name, star, limit, cursor: sqlite3.Cursor, connection: sqlite3.Connection) -> str:
    cursor.execute(sql_select_from_chara_by_name % name)
    result = cursor.fetchone()
    if result != None:
        print("学生(id: %s): %s 已经存在, 跳过" % (result[0], name))
    else:
        cursor.execute(sql_insert_into_chara % (name, star, limit))
        connection.commit()
        cursor.execute(sql_select_from_chara_by_name % name)
        result = cursor.fetchone()
        print("学生: %s 插入成功, 数据库id: %s" % (name, result[0]))
    return result[0]

if __name__ == "__main__":
    config = {}
    with open("./pool_config.cfg", "rb") as f:
        config = json.load(f)
    config = dotmap.DotMap(config)
    db_config = config.db
    db_path = db_config.path
    if not os.path.exists(db_path):
        print("没有找到数据库文件")
        exit(-1)
    connection = sqlite3.connect(db_path)
    cursor = connection.cursor()
    print("数据库连接成功")
    for action in config.action:
        if action.type == "add":
            print("%s: 执行学生添加命令..." % action.name)
            for chara in action.chara:
                name = chara.name
                star = chara.star
                limit = chara.limit
                insertStudent(name, star, limit, cursor, connection)
        if action.type == "pool":
            pool_name = action.name
            print("%s: 执行池子添加/更新命令..." % pool_name)
            # 检查池子是否存在
            cursor.execute(sql_select_from_pool_by_name % pool_name)
            update_only_flag = False
            student_ids = []
            pool = cursor.fetchone()
            if pool != None:
                print("池子: %s 已经存在, 将仅进行池子角色更新" % pool_name)
                update_only_flag = True
            else:
                cursor.execute(sql_insert_into_pool % pool_name)
                connection.commit()
                cursor.execute(sql_select_from_pool_by_name % pool_name)
                pool = cursor.fetchone()
            pool_id = pool[0]
            # 创建角色信息
            for chara in action.chara:
                name = chara.name
                star = chara.star
                limit = chara.limit
                id = insertStudent(name, star, limit, cursor, connection)
                student_ids.append((id, name))
            # 关联池子和学生
            for student in student_ids:
                cursor.execute(sql_select_from_pool_student_by_id % (pool_id, student[0]))
                record = cursor.fetchone()
                if record != None:
                    print("池子: %s 已与学生: %s 关联, 跳过" % (pool[1], student[1]))
                    continue
                cursor.execute(sql_insert_into_pool_student % (pool_id, student[0]))
                connection.commit()
            update_name = "创建"
            if update_only_flag:
                update_name = "更新"
            name_list = list(map(lambda pair: pair[1], student_ids))
            print("池子: %s %s成功, 数据库id: %s, 关联的学生: %s" % (pool_name, update_name, pool_id, ",".join(name_list)))
                
