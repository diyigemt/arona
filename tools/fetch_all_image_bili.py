import re
from bs4 import BeautifulSoup
import requests
from tools import draw_image

## 从b站专栏获取夜喵的杂图

img_folder = "image/some/"
debug_index = -1
cvs = ['cv20560474', 'cv20557188', 'cv17678326', 'cv20550762', 'cv20550621', 'cv20550020']
base_url = "https://www.bilibili.com/read/%s"
headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36"}

if __name__ == "__main__":
    index = 0
    total_index = 0
    for cv in cvs:
        url = base_url % cv
        web_data = requests.get(url=url, headers=headers)
        body = BeautifulSoup(web_data.text).body
        main_contain = list(body.select(".img-box"))
        index = 0
        for item in main_contain:
            try:
                # if debug_index == index:
                #     print("a")
                image = item.select("img")[0]
                image_url = "https://" + image.attrs["data-src"].replace("//", "").replace("\'", "")
                outer = item.previous_sibling
                names = outer.get_text()
                # 意料之外的多余空行
                if len(names) < 2:
                    names = outer.previous_sibling.get_text()
                # 下载图片
                path, hash = draw_image(image_url, "%s.png" % names, img_folder)

                index = index + 1
                print(names)
            except Exception as e:
                debug_index = index
                print(index)
                print(e)
        print(index)
        total_index += index
    print("total: %d" % total_index)