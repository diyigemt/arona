import json
from urllib.parse import quote, urlencode
import requests
from playwright.sync_api import Playwright, sync_playwright
import urllib
import urllib.request
from PIL import ImageDraw, ImageFont, Image
import cv2
import numpy as np
import re
import time
headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36"}
font_size = 28
fnt = ImageFont.truetype('C:\Windows\Fonts\msyh.ttc', font_size)
def run(playwright: Playwright):
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()

    # 从schaledb获取所有学生用于描述的姓名
    page.goto("https://lonqie.github.io/SchaleDB")

    student_list_link_btn = page.query_selector("#ba-navbar-link-students")
    student_list_link_btn.click()
    time.sleep(3)
    student_list_btn = page.query_selector("#ba-student-list-btn")
    student_list_btn.click()

    re_replace = re.compile("loadStudent\('([\w_]+)'\)")
    res = re.sub(re_replace, r"\1", "loadStudent('Akari')")

    student_loma = list(map(lambda div: re.sub(re_replace, r"\1", div.get_attribute("onclick")), page.query_selector_all(".card-student")))
    student_dict = {}
    # 获取schaledb的描述信息
    for loma in student_loma:
        page.goto("https://lonqie.github.io/SchaleDB/?chara=%s" % loma)
        stu_name = page.query_selector("#ba-student-name").text_content()
        remote = query_remote_name(stu_name)
        path = str(remote["path"])
        png_name = path.replace("/student_rank/", "")
        name_list = png_name.replace(".png", "").split("_")
        first_name = name_list[0]
        if not test_name_exist(first_name):
            first_name = name_list[1]
        student_dict[first_name] = loma

    page.goto("https://ba.game-db.tw/")

    # 切换语言
    page.locator("svg").click()
    page.locator("#react-select-2-option-1").click()
    page.get_by_text("一覧表").click()

    target_class = page.get_by_text("優香(体操服)").get_attribute("class")
    for name in page.query_selector_all(".%s" % target_class):
        name.click()
        

        # 下载远端图片
        remote = query_remote_name(name.text_content())
        path = str(remote["path"])
        png_name = path.replace("/student_rank/", "")
        name_list = png_name.replace(".png", "").split("_")
        first_name = name_list[0]
        if not test_name_exist(first_name):
            first_name = name_list[1]
            name_list.remove(first_name)
            png_name = "_".join(name_list) + ".png"
        local_path = "./image/parse/%s" % png_name
        # 从远端下载原图
        source_im = download_image("https://arona.cdn.diyigemt.com/image", path, local_path)

        # 从shaledb下载
        fetch_data_from_schaledb(student_dict[first_name])

        # 直接查看所需资源
        skill_resource_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[1]/div[2]/div[2]")
        skill_resource_btn.click()
        
        skill_resource_1 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[4]")
        skill_resource_1.screenshot(path="./image/tmp/skill_resource_1.png")
        skill_resource_2 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[5]")
        skill_resource_2.screenshot(path="./image/tmp/skill_resource_2.png")
        skill_resource_3 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[6]")
        skill_resource_3.screenshot(path="./image/tmp/skill_resource_3.png")
        skill_resource_4 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[7]")
        skill_resource_4.screenshot(path="./image/tmp/skill_resource_4.png")
        skill_resource_5 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[8]")
        skill_resource_5.screenshot(path="./image/tmp/skill_resource_5.png")
        resource_1 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[9]")
        resource_1.screenshot(path="./image/tmp/resource_1.png")
        resource_2 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[10]")
        resource_2.screenshot(path="./image/tmp/resource_2.png")
        resource_3 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[11]")
        resource_3.screenshot(path="./image/tmp/resource_3.png")

        equipment_resource_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[3]/div/div[2]")
        equipment_resource_btn.click()
        equipment_resource = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[4]")
        equipment_resource.screenshot(path="./image/tmp/equipment_resource.png")

        skill_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[1]/div/div[2]")
        skill_btn.click()

        ex_skill = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[1]")
        ex_skill.screenshot(path="./image/tmp/ex_skill.png")
        base_skill = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[2]")
        base_skill.screenshot(path="./image/tmp/base_skill.png")
        enhance_skill = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[1]")
        enhance_skill.screenshot(path="./image/tmp/enhance_skill.png")
        sub_skill = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[2]")
        sub_skill.screenshot(path="./image/tmp/sub_skill.png")

        # outer.screenshot(path="./resource.png")
        # skill = page.query_selector(".enabled").query_selector("xpath=../..").query_selector_all("div")[2]
        # skill.click()
        # outer.screenshot(path="./skill.png")
        # 下载远端图片
        # remote = query_remote_name(name.text_content())
        # path = str(remote["path"])
        # png_name = path.replace("/student_rank/", "")
        # name_list = png_name.replace(".png", "").split("_")
        # first_name = name_list[0]
        # if not test_name_exist(first_name):
        #     first_name = name_list[1]
        #     name_list.remove(first_name)
        #     png_name = "_".join(name_list) + ".png"
        # local_path = "./image/parse/%s" % png_name
        # # 从远端下载原图
        # source_im = download_image("https://arona.cdn.diyigemt.com/image", path, local_path)
        # # 扩张图片
        # resource_im = cv2.imread("./resource.png")
        # skill_im = cv2.imread("./skill.png")
        
        # parse_ba_game_db_image(source_im, resource_im, skill_im, local_path)
        break

    context.close()
    browser.close()

def fetch_data_from_schaledb(name):
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()
    page.goto("https://lonqie.github.io/SchaleDB/?chara=%s" % name)
    weapon_btn = page.query_selector("#ba-student-tab-weapon")
    weapon_btn.click()
    weapon_name = page.query_selector("//*[@id='ba-student-page-weapon']/div[1]")
    weapon_name.screenshot(path="./image/tmp/weapon_name.png")
    weapon_img = page.query_selector("//*[@id='ba-student-page-weapon']/div[2]")
    weapon_img.screenshot(path="./image/tmp/weapon_img.png")

    base_info_btn = page.query_selector("#ba-student-tab-profile")
    base_info_btn.click()

    name_card = page.query_selector("//*[@id='ba-student-page-profile']/div[1]")
    name_card.screenshot(path="./image/tmp/name_card.png")
    base_card = page.query_selector("//*[@id='ba-student-page-profile']/div[2]")
    base_card.screenshot(path="./image/tmp/name_card.png")
    gift = page.query_selector("//*[@id='ba-student-page-profile']/div[6]")
    gift.screenshot(path="./image/tmp/gift.png")
    furniture = page.query_selector("//*[@id='ba-student-page-profile']/div[8]")
    furniture.screenshot(path="./image/tmp/furniture.png")
    context.close()
    browser.close()

def parse_ba_game_db_image(source_im, resource_im, skill_im, path):
    source_rows, source_cols, _ = source_im.shape
    resource_rows, resource_cols, _ = resource_im.shape
    skill_rows, skill_cols, _ = skill_im.shape

    # # 获取最大宽高
    # rows = max(max(source_rows, resource_rows), skill_rows)
    # cols = source_cols + resource_cols + skill_cols
    # # 创建背景图 加个padding
    # final = Image.new('RGB', (cols + 30 * 2, rows), color='white')
    # # 写入原图
    # final.save(path)
    # final = cv2.imdecode(np.fromfile(path, dtype=np.uint8), -1)
    # final[0:source_rows, 0:source_cols] = source_im
    # final[0:resource_rows, source_cols + 30:source_cols + 30 + resource_cols] = resource_im
    # final[0:skill_rows, source_cols + 30 + resource_cols + 30:source_cols + 30 + resource_cols + 30 + skill_cols] = skill_im
    # cv2.imencode(".png", final)[1].tofile(path)

    # # 获取最大宽高
    # rows = max(source_rows, resource_rows + skill_rows + 30)
    # cols = source_cols + max(resource_cols, skill_cols)
    # # 创建背景图 加个padding
    # final = Image.new('RGB', (cols + 30, rows), color='white')
    # # 写入原图
    # final.save(path)
    # final = cv2.imdecode(np.fromfile(path, dtype=np.uint8), -1)
    # final[0:source_rows, 0:source_cols] = source_im
    # final[0:resource_rows, source_cols + 30:source_cols + 30 + resource_cols] = resource_im
    # final[resource_rows + 30:resource_rows + 30 + skill_rows, source_cols + 30:source_cols + 30 + skill_cols] = skill_im
    # cv2.imencode(".png", final)[1].tofile(path)

    # 获取最大宽高
    rows = source_rows + max(resource_rows, skill_rows) + 30
    cols = max(source_cols, resource_cols + skill_cols + 30)
    # 创建背景图 加个padding
    final = Image.new('RGB', (cols + 30, int(rows + font_size * 1.5)), color='white')
    draw = ImageDraw.Draw(final)
    draw.text((0 + 10, rows), "数据来源: https://ba.game-db.tw/", font=fnt, fill=(0,0,0))
    # 写入原图
    final.save(path)
    final = cv2.imdecode(np.fromfile(path, dtype=np.uint8), -1)
    final[0:source_rows, 0:source_cols] = source_im
    final[source_rows + 30:source_rows + 30 + resource_rows, 0:resource_cols] = resource_im
    final[source_rows + 30:source_rows + 30 + skill_rows, resource_cols + 30:resource_cols + 30 + skill_cols] = skill_im
    cv2.imencode(".png", final)[1].tofile(path)

def download_image(base_url: str, name, path: str):
    request = urllib.request.Request(url=base_url + quote(name),headers=headers)
    response = urllib.request.urlopen(request)
    img = response.read()
    with open(path, "wb") as f:
        f.write(img)
    return cv2.imdecode(np.fromfile(path, dtype=np.uint8), -1)

def test_name_exist(name):
    header = {
        "Content-Type": "application/json"
    }
    resp = requests.get("https://arona.diyigemt.com/api/v1/image?name=%s" % name, headers=header)
    result = json.loads(resp.content.decode())
    return len(result["data"]) == 1

def query_remote_name(name):
    header = {
        "Content-Type": "application/json"
    }
    resp = requests.get("https://arona.diyigemt.com/api/v1/image?name=%s" % name, headers=header)
    result = json.loads(resp.content.decode())
    if len(result["data"]) != 1:
        return query_remote_name(result["data"][0]["name"])
    return result["data"][0]

with sync_playwright() as playwright:
    run(playwright)