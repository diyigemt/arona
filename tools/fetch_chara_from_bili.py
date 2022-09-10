import re
from bs4 import BeautifulSoup
import requests
import zhconv
import sqlite3
from tools import replace_name, draw_image, insert_into_db
file_path = r"C:\Users\qwe13\Desktop\data.db"
img_folder = "image/student_rank/"
debug_index = -1
cvs = ['cv15830417', 'cv15832875', 'cv15833934', 'cv15839738']
# cvs = ['cv15839738']
base_url = "https://www.bilibili.com/read/%s"
headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36"}
copy_suffix = ["幼女", "私服", "泳装", "温泉", "自行车装", "正月", "兔女郎"]
copy_match = re.compile("\((.*)\)")

if __name__ == "__main__":
    connection = sqlite3.connect(file_path)
    cursor = connection.cursor()
    index = 0
    total_index = 0
    print("数据库连接成功")
    for cv in cvs:
        url = base_url % cv
        web_data = requests.get(url=url, headers=headers)
        body = BeautifulSoup(web_data.text).body
        main_contain = list(body.select(".img-box"))
        main_contain.reverse()
        main_contain.pop()
        index = 0
        for item in main_contain:
            try:
                if debug_index == index:
                    print("a")
                image = item.select("img")[0]
                image_url = "https://" + image.attrs["data-src"].replace("//", "").replace("\'", "")
                outer = item.previous_sibling
                names = outer.get_text()
                # 意料之外的多余空行
                if len(names) < 2:
                    names = outer.previous_sibling.get_text()
                names = names.replace("（", "(")
                names = names.replace("）", ")")
                names = names.replace("\xa0\xa0", " ")
                names = names.replace("\xa0", " ")
                names = zhconv.convert(names, "zh-cn").split(" ")
                name_prefix = names[0]
                name_suffix = names[1]
                # if name_suffix.find("枫") != -1:
                #     print("a")
                # 替换调黏在一起的入手管道
                if name_suffix.find("入") != -1:
                    name_suffix = name_suffix[0:name_suffix.find("入")]
                if len(name_prefix) > 3:
                    name_suffix = name_prefix[2:4]
                    name_prefix = name_prefix[0:2]
                if len(name_suffix) > 3 and name_suffix.find("(") == -1 and name_suffix.find("/") == -1:
                    name_suffix = name_prefix[2:3]
                    name_prefix = name_prefix[0:2]
                # 多翻译替换
                if name_suffix.find("(") != -1:
                    target = copy_match.search(name_suffix).group(1)
                    # 说明是多翻译而不是复制人
                    if target not in copy_suffix:
                        name_suffix = name_suffix.replace("(%s)" % target, "/%s" % target)
                    # 给复制人的多翻译加上后缀
                    else:
                        name_suffix = name_suffix.replace("/", "(%s)/" % target)
                # 别名链接
                more_name = replace_name.get(name_suffix)
                if more_name != None:
                    name_suffix = name_suffix + more_name
                #　图片路径问题
                name_suffix = name_suffix.replace("/", "_")
                file_name = "%s_%s" % (name_prefix, name_suffix)
                # 下载图片
                path, hash = draw_image(image_url, "%s.png" % file_name)
                # 将数据插入数据库中
                insert_into_db(file_name, hash, img_folder, cursor, connection)

                index = index + 1
                print("%s %s" % (name_prefix, name_suffix))
            except Exception as e:
                debug_index = index
                print(index)
                print(e)
        print(index)
        total_index += index
    print("total: %d" % total_index)