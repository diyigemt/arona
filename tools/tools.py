import urllib
from PIL import ImageDraw, ImageFont, Image
import cv2
import hashlib
import sqlite3
headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36"}
tmp_file_path = "tmp.png"
tmp_file_path2 = "tmp2.png"
img_folder = "image/student_rank/"
source_str = "图片来源: 巴哈姆特@夜喵貓貓咪喵(asaz5566a)"
font_size = 28
fnt = ImageFont.truetype('C:\Windows\Fonts\msyh.ttc', font_size)
def draw_image(url: str, name: str):
    request = urllib.request.Request(url=url,headers=headers)
    response = urllib.request.urlopen(request)
    img = response.read()
    with open(tmp_file_path, "wb") as f:
        f.write(img)
    img = cv2.imread(tmp_file_path)
    rows, cols, _ = img.shape
    bg = Image.new('RGB', (cols, int(rows + font_size * 1.5)), color='white')
    draw = ImageDraw.Draw(bg)
    draw.text((0 + 10, rows), source_str, font=fnt, fill=(0,0,0))
    bg.save(tmp_file_path2)
    bg = cv2.imread(tmp_file_path2)
    bg[0:rows, 0:cols] = img
    final_path = img_folder + name
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

def update_alias(id: str, alias: list[str], cursor: sqlite3.Cursor, connection: sqlite3.Connection):
    for a in alias:
        a = replace0(a)
        cursor.execute("SELECT * FROM `image` WHERE `name` = '%s'" % a)
        history = cursor.fetchone()
        if history != None:
            cursor.execute("UPDATE `image` SET `path` = '%s', `hash` = '%s'" % (id, id))
        else:
            cursor.execute("INSERT INTO `image`(`name`, `path`, `hash`) VALUES ('%s', '%s', '%s')" % (a, id, id))
        connection.commit() 

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
        cursor.execute("UPDATE `image` SET `path` = '%s', `hash` = '%s'" % (path, hash))
    else:
    # 否则新建记录   
        cursor.execute("INSERT INTO `image`(`name`, `path`, `hash`) VALUES ('%s', '%s', '%s')" % (last_name, path, hash))
    connection.commit()
    if len(names) > 2:
        alias = names[2: len(names)]
        cursor.execute("SELECT * FROM `image` WHERE `hash` = '%s'" % hash)
        id = cursor.fetchone()[0]
        update_alias(id, alias, cursor, connection)

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