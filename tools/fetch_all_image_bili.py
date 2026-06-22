import re
from bs4 import BeautifulSoup
import requests
from tools import draw_image
import threading

## 从b站专栏获取夜喵的杂图

img_folder = "image/some/"
debug_index = -1
cvs = ['1214073455168716821', '1214143476040466455', '1214440687747989511', '1214762784537968695', '1215107301055135767', '1215489492984528915', '1216413726981226515']
base_url = "https://www.bilibili.com/opus/%s"
headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36"}

def download(cv: str):
    url = base_url % cv
    web_data = requests.get(url=url, headers=headers)
    body = BeautifulSoup(web_data.text).body
    main_contain = list(body.select(".opus-para-pic"))
    index = 0
    for item in main_contain:
        try:
            # if debug_index == index:
            #     print("a")
            image = item.select("img")[0]
            image_url = "https://" + image.attrs["src"].replace("//", "").replace("\'", "")
            outer = item.previous_sibling
            names = outer.get_text().replace("/", "-")
            # 意料之外的多余空行
            if len(names) < 2:
                names = outer.previous_sibling.get_text()
            names = names.split(" ")[0].strip()
            names = names.split("\xa0")[0].strip()
            names = names.split("\ufeff")[0].strip()
            if names.find("(") != -1:
                names = re.sub(r'([^(]+)\(([^)]+)\)', r'\2\1', names)
            # 下载图片
            path, hash = draw_image(image_url, "%s.png" % names, img_folder, "bilbilibili@校委会攻略组(259878)")

            index = index + 1
            print(names)
        except Exception as e:
            debug_index = index
            print(index)
            print(e)


if __name__ == "__main__":
    index = 0
    total_index = 0
    threads = [threading.Thread(target=download, args=(cv,)) for cv in cvs]
    print("start with %d threads" % len(threads))
    for t in threads:
        t.start()
    for t in threads:
        t.join()