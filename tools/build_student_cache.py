import codecs
import json
import re
import os
import time
from playwright.sync_api import Playwright, sync_playwright
from fetch_student_info_from_ba_game_db import query_remote_name, replace_none_char
from config import cache_file_location, name_map_dict_file_location
game_kee_prefix = "https://ba.gamekee.com"
name_map_dict = {}
def run(playwright: Playwright):
    build_name_map_dict()
    build_game_kee_student_mapping(playwright)
    build_stu_loma_map_from_schaledb(playwright)
    check_ba_game_db_name_mapping(playwright)

def build_stu_loma_map_from_schaledb(playwright: Playwright):
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080})
    page = context.new_page()

    # 加载缓存
    cache_dict = {}
    with codecs.open(cache_file_location, "r", encoding="utf-8") as f:
    # todo
        pass
        #cache_dict = json.loads(f.read())

    # 从schaledb获取所有学生用于描述的姓名
    page.goto("https://lonqie.github.io/SchaleDB")

    # 关闭change-log窗口
    model = page.query_selector("#modal-changelog")
    if model != None:
        close_btn = model.query_selector(".btn-close")
        if close_btn != None:
            close_btn.click()

    # 换成日服中文
    setting_btn = page.query_selector("#ba-navbar-regionselector")
    setting_btn.click()
    region_btn = page.query_selector("#ba-navbar-regionselector-0")
    region_btn.click()
    language_btn = page.query_selector("#ba-navbar-languageselector")
    language_btn.click()
    language_cn_btn = page.query_selector("#ba-navbar-languageselector-cn")
    language_cn_btn.click()
    stu_page_btn = page.query_selector("#ba-navbar-link-students")
    stu_page_btn.click()
    time.sleep(3)
    student_list_btn = page.query_selector("#ba-student-list-btn")
    student_list_btn.click()

    re_replace = re.compile("loadStudent\('([\w_]+)'\)")

    # 加载别名防止后端返回结果出错
    name_map_dict = {}
    if os.path.exists(name_map_dict_file_location):
        with open(name_map_dict_file_location, "r", encoding="UTF-8") as f:
            for cache in f.readlines():
                cache = cache.replace("\n", "")
                cache = cache.replace("\r", "")
                if len(cache) == 0:
                    continue
                sp = cache.split(",")
                name_map_dict[sp[0]] = sp[1]
    # 第一步 获取罗马音与远端中文名的对应关系
    stu_loma_dict = {}
    # 获取schaledb的描述信息
    for item in page.query_selector_all(".card-student"):
        loma = re.sub(re_replace, r"\1", item.get_attribute("onclick"))
        name = item.query_selector(".card-label").query_selector(".label-text").text_content()
        # 人工替换可以识别的文字
        remote_name = get_cn_name_from_remote(name)
        # 理论上不会发生
        if loma in stu_loma_dict:
            print("crash: local: %s, loma: %s, remote: %s" % (name, loma, remote_name))
            exit(-1)
        else:
            print("success: %s,%s" % (remote_name, loma))
            stu_loma_dict[loma] = remote_name
    # 第二步 构建日文名与中文名和罗马音的关系
    # 换成日服日文
    # 点一下关闭filter
    page.query_selector(".card-student").click()
    time.sleep(3)
    setting_btn = page.query_selector("#ba-navbar-regionselector")
    setting_btn.click()
    region_btn = page.query_selector("#ba-navbar-regionselector-0")
    region_btn.click()
    language_btn = page.query_selector("#ba-navbar-languageselector")
    language_btn.click()
    language_cn_btn = page.query_selector("#ba-navbar-languageselector-jp")
    language_cn_btn.click()
    time.sleep(3)
    student_list_btn = page.query_selector("#ba-student-list-btn")
    student_list_btn.click()

    for item in page.query_selector_all(".card-student"):
        loma = re.sub(re_replace, r"\1", item.get_attribute("onclick"))
        jpName = item.query_selector(".card-label").query_selector(".label-text").text_content()
        jpName = replace_none_char(jpName)
        # 理论上不会发生
        if jpName in stu_loma_dict:
            print("crash: jpName: %s, loma: %s, cnName: %s" % (jpName, loma, stu_loma_dict[loma]))
            exit(-1)
        else:
            if jpName in cache_dict:
                print("skip: jpName: %s, loma: %s, cnName: %s" % (jpName, loma, stu_loma_dict[loma]))
            else:
                print("append: %s,%s" % (jpName, stu_loma_dict[loma]))
                cache_dict[jpName] = {
                "cnName": stu_loma_dict[loma],
                "loma": loma
                }
    # 通过中文名关联gamekee页面
    game_kee_dict = build_game_kee_student_mapping(playwright)
    for key in cache_dict:
        item = cache_dict[key]
        cnName = item["cnName"]
        if cnName in game_kee_dict:
            item["gamekee"] = game_kee_dict[cnName]
        else:
            print("game cn name: %s not found match" % cnName)

    if os.path.exists(cache_file_location):
        os.remove(cache_file_location)
    with codecs.open(cache_file_location, "w", encoding="utf-8") as f:
        f.write(json.dumps(cache_dict, ensure_ascii=False))
        f.flush()
    context.close()
    browser.close()

def check_ba_game_db_name_mapping(playwright: Playwright):
    stu_final_dict = {}
    with codecs.open(cache_file_location, "r", encoding="utf-8") as f:
        stu_final_dict = json.load(f)

    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()
    page.goto("https://ba.game-db.tw/")

    # 切换语言
    page.locator("svg").click()
    page.locator("#react-select-2-option-0").click()
    page.get_by_text("一覧表").click()

    target_class = page.get_by_text("ユウカ（体操服）").get_attribute("class")
    success_count = 0
    error_count = 0
    for item in page.query_selector_all(".%s" % target_class):
        name = item.text_content()
        name = replace_none_char(name)
        if name in stu_final_dict:
            print("success match: %s" % name)
            success_count = success_count + 1
        else:
            print("error match: %s" % name)
            error_count = error_count + 1
    print("success: %d/118" % success_count)
    print("error: %d/118" % error_count)
    context.close()
    browser.close()

def build_game_kee_student_mapping(playwright: Playwright):
    student_dict = {}
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080})
    page = context.new_page()
    page.goto("https://ba.gamekee.com/")
    time.sleep(2)

    container = page.query_selector('//*[@id="menu-23941"]/div[3]/div[2]/div[1]/div[2]')
    for item in container.query_selector_all("a"):
        href = item.get_attribute("href")
        student_name = replace_none_char(item.get_attribute("title"))
        # 替换人工名字
        student_name = get_cn_name_from_remote(student_name)
        student_dict[student_name] = game_kee_prefix + href
    context.close()
    browser.close()
    return student_dict

def get_cn_name_from_remote(name: str) -> str:
    # 人工替换正确名字
    name = replace_none_char(name)
    if name in name_map_dict:
        name = name_map_dict[name]
    remote = query_remote_name(name)
    return str(remote["name"])

def build_name_map_dict():
    if os.path.exists(name_map_dict_file_location):
        with open(name_map_dict_file_location, "r", encoding="UTF-8") as f:
            for cache in f.readlines():
                cache = cache.replace("\n", "")
                cache = cache.replace("\r", "")
                if len(cache) == 0:
                    continue
                sp = cache.split(",")
                name_map_dict[sp[0]] = sp[1]

if __name__ == "__main__":
    with sync_playwright() as playwright:
        run(playwright)