from bs4 import BeautifulSoup
import requests
headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36"}
BASE_URL = "https://bluearchive.wikiru.jp/?"
BASE_IMAGE_URL = "https://bluearchive.wikiru.jp/"
CHAPTER_NAME = "%E7%AB%A0"
TABLE_FLAGE = "rgn_content"
def generate_sub_chapter_list(chapter: int) -> list[str]:
    list = []
    c = str(chapter)
    list.append(c + "-1")
    list.append(c + "-2")
    list.append(c + "-3")
    list.append(c + "-4")
    list.append(c + "-5")
    list.append("H" + c + "-1")
    list.append("H" + c + "-2")
    list.append("H" + c + "-3")
    return list

def main_map(r: range):
    for i in r:
        for suffix in generate_sub_chapter_list(i):
            url = "%s%d%s/%s" % (BASE_URL, i, CHAPTER_NAME, suffix)
            web_data = requests.get(url=url, headers=headers)
            body = BeautifulSoup(web_data.text).body
            test_index = 1
            container = body.select_one("#%s%d" % (TABLE_FLAGE, test_index))
            if container == None or len(container.select(".style_table")) == 0:
                test_index = test_index + 1
                container = body.select_one("#%s%d" % (TABLE_FLAGE, test_index))
            if container == None:
                print("生成:%d 时失败" % suffix)
                continue
            step_table = container.select(".style_table")
            if len(step_table) == 0:
                test_index = test_index + 1
                container = body.select_one("#%s%d" % (TABLE_FLAGE, test_index))
                step_table = container.select_one(".style_table")
            else:
                step_table = step_table[0]
            img = container.select("img")
            

    pass
