import re
from bs4 import BeautifulSoup
import requests
import zhconv
from PIL import ImageDraw, ImageFont, Image
import cv2
import urllib
from tools import replace_name
debug_index = -1
url = "https://forum.gamer.com.tw/C.php?bsn=38898&snA=698&tnum=34"
headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36"}
copy_suffix = ["幼女", "私服", "泳装", "温泉", "自行车装", "正月", "兔女郎"]
copy_match = re.compile("\((.*)\)")
tmp_file_path = "tmp.png"
tmp_file_path2 = "tmp2.png"
font_size = 28
source = "图片来源: 巴哈姆特@夜喵貓貓咪喵(asaz5566a)"
fnt = ImageFont.truetype('C:\Windows\Fonts\msyh.ttc', font_size)
def draw_image(url, name):
    request = urllib.request.Request(url=url,headers=headers)
    response = urllib.request.urlopen(request)
    img = response.read()
    with open(tmp_file_path, "wb") as f:
        f.write(img)
    img = cv2.imread(tmp_file_path)
    rows, cols, _ = img.shape
    bg = Image.new('RGB', (cols, int(rows + font_size * 1.5)), color='white')
    draw = ImageDraw.Draw(bg)
    draw.text((0 + 10, rows), source, font=fnt, fill=(0,0,0))
    bg.save(tmp_file_path2)
    bg = cv2.imread(tmp_file_path2)
    bg[0:rows, 0:cols] = img
    cv2.imencode(".png", bg)[1].tofile("image/student_rank/" + name)
if __name__ == "__main__":
    web_data = requests.get(url=url, headers=headers)
    body = BeautifulSoup(web_data.text).body
    main_contain = list(body.select(".c-article__content"))
    main_contain.reverse()
    main_contain.pop()
    images = [i for item in (list(map(lambda item: item.select(".photoswipe-image"), main_contain))) for i in item]
    index = 0
    for a in images:
        try:
            if debug_index == index:
                print("a")
            image = a.select("img")[0]
            image_url = image.attrs["data-src"]
            outer = a.parent.previous_sibling
            if outer == None:
                if a.parent.name == "font":
                    outer = a.parent.parent
                    if outer.name == "a":
                        outer = outer.parent.parent.previous_sibling
                    else:
                        outer = outer.previous_sibling
                else:
                    outer = a.parent.parent.previous_sibling
                    outer = outer.contents
                    outer = outer[len(outer) - 1]
                    outer = outer.contents
                    outer = outer[len(outer) - 1]
            if outer.name == "a":
                outer = outer.parent.parent.previous_sibling
            while len(outer.contents) > 1 and outer.contents[0].name == "div":
                outer = outer.contents[len(outer.contents) - 1]
            names = outer.get_text()
            if len(names) == 0:
                outer = outer.previous_sibling
                names = outer.get_text()
            names = names.replace("（", "(")
            names = names.replace("）", ")")
            names = names.replace("\xa0\xa0", " ")
            names = names.replace("\xa0", " ")
            names = zhconv.convert(names, "zh-cn").split(" ")
            name_prefix = names[0]
            name_suffix = names[1]
            # if name_suffix.find("兔女郎") != -1:
            #     print("a")
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
            draw_image(image_url, "%s.png" % file_name)
            index = index + 1
            print("%s %s" % (name_prefix, name_suffix))
        except Exception as e:
            debug_index = index
            print(index)
            print(e)
    print(index)