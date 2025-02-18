import shutil
import urllib
import urllib.request
from PIL import ImageDraw, ImageFont, Image
import cv2
import hashlib
import sqlite3
import os
import re
import getpass
import requests
import paramiko
import json
import numpy as np
import uuid

from reflash_cdn import purgeFiles
password_file = "C:\\Users\\%s\\.ssh\\arona-backend-password" % getpass.getuser()
from zhon.hanzi import punctuation
headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36"}
tmp_file_path = "tmp.png"
tmp_file_path2 = "tmp2.png"
img_folder = "image/student_rank/"
source_str = "巴哈姆特@夜喵貓貓咪喵(asaz5566a)"
# offset 几行字的偏移量默认是1
def draw_image_source(path: str, source: str, offset = 1):
    # 一律转换为png
    img = Image.open(path)
    img = img.convert("RGBA", colors=255)
    cols = img.width
    rows = img.height
    font_size = max(12, cols / 50)
    font_size_big = max(12, cols / 40)
    if offset >= 1:
        mr = int(rows + font_size * 1.5 + font_size_big * 1.5)
        bg = Image.new('RGBA', (cols, mr), color='white')
    else:
        bg = Image.new('RGBA', (cols, rows), color='white')
    bg.paste(img, (0,0))
    draw = ImageDraw.Draw(bg)
    fnt = ImageFont.truetype('C:\Windows\Fonts\msyh.ttc', font_size)
    fnt_big = ImageFont.truetype('C:\Windows\Fonts\msyh.ttc', font_size_big)
    draw.text((0 + 10, rows + (1 - 1) * font_size * 1.5), source, font=fnt, fill=(0,0,0))
    draw.text((0 + 10, rows + (2 - 1) * font_size * 1.5), "角色评分已停止更新，最后更新时间：2025-02-18", font=fnt_big, fill=(255,0,0))
    bg.save(path)
    bg = cv2.imdecode(np.fromfile(path, dtype=np.uint8), -1)
    return bg
## 获取图片并在图片上添加图片来源
def draw_image(url: str, name: str, override_path: str = "", source = source_str):
    request = urllib.request.Request(url=url,headers=headers)
    response = urllib.request.urlopen(request)
    img = response.read()
    tmp_file_path_random = f"./image/tmp/{str(uuid.uuid4())}.png"
    with open(tmp_file_path_random, "wb") as f:
        f.write(img)
    bg = draw_image_source(tmp_file_path_random, "图片来源: " + source)
    os.remove(tmp_file_path_random)
    name = re.sub('[\/:*?"<>|]','',name) # 移除非法字符
    name = re.sub(r"[%s]+" %punctuation, "",name)
    final_path = img_folder + name
    if override_path != "":
        final_path = override_path + name

    cv2.imencode(".png", bg)[1].tofile(final_path)
    # 计算md5
    hash = ""
    with open(final_path, "rb") as f:
        hash = hashlib.md5(f.read()).digest().hex()
    return final_path, hash
def replace0(source):
    first = source.find("(")
    last = source.find(")")
    if first != -1:
        source = source[first+1: last] + source[0:first]
    return source

def update_alias(id: str, alias: list[str], cursor: sqlite3.Cursor, connection: sqlite3.Connection, type: int = 1):
    for a in alias:
        a = replace0(a)
        cursor.execute("SELECT * FROM `image` WHERE `name` = '%s'" % a)
        history = cursor.fetchone()
        if history != None:
            id0 = str(history[0])
            cursor.execute("UPDATE `image` SET `path` = '%s', `hash` = '%s' WHERE `id` = '%s'" % (id, id, id0))
        else:
            cursor.execute("INSERT INTO `image`(`name`, `path`, `hash`, `type`) VALUES ('%s', '%s', '%s', %d)" % (a, id, id, type))
        connection.commit()
    # 删除弃用的名字
    cursor.execute("SELECT * FROM `image` WHERE `path` = '%s'" % id)
    for item in cursor.fetchall():
        item_name = str(item[1])
        # 别名已弃用
        if item_name not in alias:
            target_id = str(item[0])
            cursor.execute("DELETE FROM `image` WHERE `id` = '%s'" % target_id)

def insert_into_db(name: str, hash: str, folder: str, cursor: sqlite3.Cursor, connection: sqlite3.Connection):
    path = folder + name
    if path.find(".png") == -1:
        path = path + ".png"
    divs = name.replace(".png", "")
    names = divs.split("_")
    first_name = names[0]
    last_name = names[1]
    last_name = replace0(last_name)
    # 检查是否有记录
    cursor.execute("SELECT * FROM `image` WHERE `hash` = '%s' OR `name` = '%s'" % (hash, last_name))
    history = cursor.fetchone()
    # 有记录 更新名字和hash
    if history != None:
        id = str(history[0])
        cursor.execute("UPDATE `image` SET `path` = '%s', `hash` = '%s' WHERE `id` = '%s'" % (path, hash, id))
    else:
    # 否则新建记录   
        cursor.execute("INSERT INTO `image`(`name`, `path`, `hash`, `type`) VALUES ('%s', '%s', '%s', 1)" % (last_name, path, hash))
    connection.commit()
    if len(names) > 2:
        alias = names[2: len(names)]
        cursor.execute("SELECT * FROM `image` WHERE `hash` = '%s'" % hash)
        id = cursor.fetchone()[0]
        update_alias(id, alias, cursor, connection)

base_img_folder = "image"
def update_image(folder: str, cursor: sqlite3.Cursor, connection: sqlite3.Connection, type: int = 2):
    index = 0
    for file in os.listdir(base_img_folder + folder):
        file_name = file.replace(".png", "")
        file_path = base_img_folder + folder + file
        file_path_absolute = folder + file
        
        file_names = file_name.split("_")
        
        # file_size = os.path.getsize(file_path) / 1024 / 1024 # M
        # # 将大于3M的图片进行压缩
        # if file_size > 3:
        #     im = Image.open(file_path)
        #     (x, y) = im.size
        #     resize = im.resize((int(x * 0.7), int(y * 0.7)), Image.ANTIALIAS)
        #     resize.save(file_path)
        # 计算md5
        hash = ""
        with open(file_path, "rb") as f:
            hash = hashlib.md5(f.read()).digest().hex()
        # 更新主记录
        main_name = file_names[0]
        cursor.execute("SELECT * FROM `image` WHERE `hash` = '%s' OR `name` = '%s'" % (hash, main_name))
        history = cursor.fetchone()
        # 有记录 更新path和hash
        if history != None:
            id = str(history[0])
            cursor.execute("UPDATE `image` SET `name` = '%s', `path` = '%s', `hash` = '%s' WHERE `id` = '%s'" % (main_name, file_path_absolute, hash, id))
            connection.commit()
        else:
            # 否则新建记录   
            cursor.execute("INSERT INTO `image`(`name`, `path`, `hash`, `type`) VALUES ('%s', '%s', '%s', %d)" % (file_name, file_path_absolute, hash, type))
            connection.commit()
            cursor.execute("SELECT * FROM `image` WHERE `hash` = '%s'" % hash)
            history = cursor.fetchone()
        # 如果有别名
        if len(file_names) > 1:
            file_names.remove(main_name)
            main_id = str(history[0])
            update_alias(main_id, file_names, cursor, connection, type=type)
            # for alias in file_names:
            #     cursor.execute("SELECT * FROM `image` WHERE `name` = '%s'" % alias)
            #     history = cursor.fetchone()
            #     # 有记录 更新path和hash
            #     if history != None:
            #         id = str(history[0])
            #         cursor.execute("UPDATE `image` SET `name` = '%s', `path` = '%s', `hash` = '%s' WHERE `id` = '%s'" % (alias, main_id, main_id, id))
            #     else:
            #     # 否则新建记录   
            #         cursor.execute("INSERT INTO `image`(`name`, `path`, `hash`, `type`) VALUES ('%s', '%s', '%s', %d)" % (alias, main_id, main_id, type))
            #     connection.commit()
        index += 1
    return index

if __name__ == "__main__":
    draw_image("https://i0.hdslb.com/bfs/new_dyn/a855fcd158a9ba681ba8ac6bb5c9954f425535005.png", "生盐_诺亚.png", "./image/some/")

def get_password() -> str:
    pw = ""
    with open(password_file, "r") as f:
        pw = str(f.read())
    return pw

type_str_map = {
    1: "student",
    2: "map",
    3: "other"
}

def update_image_from_api(folder: str, type: int = 2):
    index = 0
    dict = []
    for file in os.listdir(base_img_folder + folder):
        if not file.endswith(".png"):
            continue
        file_name = file.replace(".png", "")
        file_path = base_img_folder + folder + file
        file_compress_for_guild_path = base_img_folder + "/s" + folder + file
        file_path_absolute = folder + file
        
        file_names = list(map(lambda n: replace0(n), file_name.split("_")))
        # webp压缩
        # im.save(file_path, format="webp", quality=95, method=6)
        # 将大于5.5M的图片进行压缩
        while os.path.getsize(file_path) / 1024 / 1024 > 5.5:
            im = Image.open(file_path)
            (x, y) = im.size
            resize = im.resize((int(x * 0.95), int(y * 0.95)), Image.Resampling.LANCZOS)
            resize.save(file_path)
        # 计算md5
        hash = ""
        with open(file_path, "rb") as f:
            hash = hashlib.md5(f.read()).digest().hex()
        # 将大于4M的图片压缩供频道发送
        shutil.copy(file_path, file_compress_for_guild_path)
        while os.path.getsize(file_compress_for_guild_path) / 1024 / 1024 > 3.8:
            im = Image.open(file_compress_for_guild_path)
            (x, y) = im.size
            resize = im.resize((int(x * 0.95), int(y * 0.95)), Image.Resampling.LANCZOS)
            resize.save(file_compress_for_guild_path)
        # 插入主记录
        main_name = file_names[0]
        # 判断是否新增
        # 第一个名字不存在
        if not test_name_exist(main_name):
            # 且没有第二个名字或者第二个名字也不存在, 说明是新增, 不做操作
            if len(file_names) > 1:
                if not test_name_exist(file_names[1]):
                    pass
                else:
                    file_names.remove(main_name)
                    main_name = file_names[0]
        region = "jp"
        if main_name.find("国服") != -1:
            region = "cn"
        dict.append({
            "name": main_name,
            "path": file_path_absolute,
            "hash": hash,
            "type": type_str_map[type],
            "region": region
        })
        # 如果有别名
        if len(file_names) > 1:
            file_names.remove(main_name)
            for a in file_names:
                dict.append({
                    "name": a,
                    "path": file_path_absolute,
                    "hash": hash,
                    "type": type_str_map[type],
                    "region": region
                })
        index += 1
    if len(dict) == 0:
        print("empty!")
        return
    #信息收集完成
    print(list(map(lambda item: item["name"], dict)))
    if not confirm_action():
        exit(0)
    #提交到后端进行处理
    return dict

def post_image_to_remote(folder: str):
    cdn_path = "https://arona.cdn.diyigemt.com/image"
    purgePath = []
    index = 0
    pwd = ""
    if os.path.exists(r"C:\Users\%s\.ssh\pwd" % getpass.getuser()):
        with open(r"C:\Users\%s\.ssh\pwd" % getpass.getuser(), "r") as f:
            pwd = f.readline()
    key = paramiko.RSAKey.from_private_key_file(r"C:\Users\%s\.ssh\id_rsa" % getpass.getuser(), password=pwd)
    transport = paramiko.Transport(("42.192.117.253", 22))
    transport.connect(username="root", pkey=key)
    sftp = paramiko.SFTPClient.from_transport(transport)
    for file in os.listdir(base_img_folder + folder):
        if not file.endswith(".png"):
            continue
        file_path = base_img_folder + folder + file
        file_history_path = base_img_folder + "/history" + folder + file
        file_remote_path = folder + file
        file_compress_for_guild_path = base_img_folder + "/s" + folder + file
        remote_guild_path = "/srv/arona-backend/image/s%s" % (file_remote_path)
        remote_path = "/srv/arona-backend/image%s" % (file_remote_path)
        sftp.put(file_path, remote_path)
        sftp.put(file_compress_for_guild_path, remote_guild_path)
        purgePath.append(cdn_path + file_remote_path)
        purgePath.append(cdn_path + "/s" + file_remote_path)
        shutil.move(file_path, file_history_path)
        index += 1
    if index == 0:
        print("empty!")
        return
    purgeFiles(purgePath)
    for p in purgePath:  
        print(p)
    print("success: %d" % index)

def post_data(action: str, data: any, printResp: bool = True):
    header = {
        "token": get_password(),
        "Content-Type": "application/json"
    }
    for _ in range(0, 3):
        try:
            resp = requests.post("https://arona.diyigemt.com/api/v2/admin/action", json={
                "action": action,
                "data": data
            }, headers=header)
            if printResp:
                print(resp.text)
                return resp
        except Exception as _:
            pass
    raise Exception()
        
def test_name_exist(name):
    header = {
        "Content-Type": "application/json"
    }
    resp = requests.get("https://arona.diyigemt.com/api/v2/image?name=%s" % name, headers=header)
    result = json.loads(resp.content.decode())
    return result != None and result["data"] != None and len(result["data"]) == 1

def confirm_action(info = "process update?") -> bool:
    if str(input(f"{info} [Y/N]:")).lower() != "y":
        return False
    return True

replace_name = {
    "沙耶": "/老鼠/鼠鼠",
    "沙耶(私服)": "/滑鼠/私服老鼠/私服鼠鼠",
    "旬": "/瞬/舜/梅花狙",
    "旬(幼女)": "/幼瞬/幼舜/同花瞬/同花舜/铜花瞬/铜花舜",
    "泉奈/伊树菜": "/小狐狸",
    "月咏": "/大只女/大野/巨忍",
    "若藻(泳装)": "/水狐狸/水狐/水藻/水若藻",
    "和香(温泉)": "/老板娘",
    "切里诺/洁莉诺": "/斯大罗",
    "切里诺/洁莉诺(温泉)": "/温泉罗",
    "玛丽娜": "/保洁阿姨",
    "星乃/星野": "/大叔",
    "城子/白子": "/xcw/小仓唯",
    "城子(自行车装)": "/车白/小车唯/骑白子/骑行白子",
    "茜香(正月)": "/春黑/新春黑猫",
    "野乃美(泳装)": "/水乃美/水富婆",
    "日富美/日步美": "/鸡妹",
    "日富美(泳装)": "/水富美/水鸡妹/水日富美",
    "夏": "/小夏",
    "忧": "/ui/UI",
    "泉": "/汉堡",
    "雏/阳奈": "/日奈/魔王/老婆",
    "亚琉/亚瑠": "/阿露/啊露/阿鲁/啊鲁/社长",
    "千夏(温泉)": "/温泉夏",
    "春奈/羽榴奈": "/神秘狙/美食狙/晴奈",
    "伊织": "/佐仓/佐三枪",
    "濑名/濑奈": "/救护车/泥头车",
    "伊吕波": "/168",
    "瑠/宁瑠": "/尼禄/不良",
    "咏美/英美": "/艾米",
    "堇": "/运动妹",
    "真姬/真纪": "/彩蛋/maki/涂鸦",
    "花梨/花凛": "/黑皮",
    "明日奈(兔女郎)": "/兔丝娜",
    "翠/绿": "/小绿",
    "响": "/迫击炮",
    "美游": "/拉姬兔/垃圾兔/垃姬兔",
    "美咲": "/門香/圆香",
    "敦子": "/亚津子/公主",
    "桐乃": "/警察坦",
    "静子": "/餐车",
    "菲娜": "/不死鸟",
    "椿": "/狗盾",
    "野野美/野乃美": "/富婆/nnm",
    "芹香/茜香": "/黑猫",
    "玛莉": "/修女",
    "花江/花绘": "/大护士",
    "瀬里奈/芹奈": "/小护士",
    "莲实": "/莲见",
    "佳代子": "/恐惧枪",
    "遥/遥香": "/春香/德棍",
    "朱里/亚伽里": "/明里/牛牛",
    "纯子/淳子": "/丸子",
    "睦月/无月": "/地雷妹",
    "亚都梨": "/柯托莉/护盾妹",
    "咏叶": "/歌原/炮台",
    "明日奈": "/亚丝娜",
    "优华/优香": "/佑香/没包人",
    "桃井": "/小桃/粉猫",
    "茜/朱音": "/破甲女仆/破防女仆",
    "未来": "/初音",
    "和香": "/望远镜",
    "智惠": "/巴",
    "鹤城(泳装)/弦生(泳装)": "/水颜艺",
    "真白(泳装)/麻白(泳装)": "/水真白",
    "梓(泳装)": "/水梓",
    "泉(泳装)": "/水汉堡",
    "伊织(泳装)": "/水佐仓",
    "雏(泳装)/阳奈(泳装)": "/水魔王/水日奈/水老婆",
    "睦月(正月)/无月(正月)": "/春雷/春月/新月/春睦月",
    "亚瑠(正月)": "/春鲁/新社/春露/新春阿鲁/春社长/新春社长",
    "花梨(兔女郎)/花凛(兔女郎)": "/黑兔",
    "音瑠(兔女郎)/宁榴(兔女郎)": "/红兔",
    "吹雪": "/摆烂",
    "若藻": "/黑狐狸/大狐狸",
    "美智满": "/满/小满/部长",
    "静子(泳装)": "/水静子/水老板娘",
    "知世(泳装)": "/水千世/水千",
    "伊树菜(泳装)": "/泉奈(泳装)/水忍忍/水忍狐/水泉奈/水小狐狸",
    "绫音(泳装)": "/直升机",
    "星野(泳装)": "/水星野/水大叔",
}