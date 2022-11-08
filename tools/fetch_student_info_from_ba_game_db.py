
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
import os
import codecs
from functools import reduce
headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36"}
font_size = 28
fnt = ImageFont.truetype('C:\Windows\Fonts\msyh.ttc', font_size)
cache_file_location = r"./config/student_cache.json"
name_map_dict_file_location = r"./config/name_map_dict.txt"
def run(playwright: Playwright):
    # 检查mapping
    # build_stu_loma_map_from_schaledb(playwright)
    # check_ba_game_db_name_mapping(playwright)
    # fetch_data_from_schaledb("Yuuka_Track")
    # 开始截图
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()

    # 加载已缓存的信息
    student_dict = {}
    with codecs.open(cache_file_location, "r", encoding="utf-8") as f:
        student_dict = json.load(f)
    
    # 拿到成长资源截图
    page.goto("https://ba.game-db.tw/")

    # 切换语言
    page.locator("svg").click()
    page.locator("#react-select-2-option-0").click()
    page.get_by_text("一覧表").click()
    # 拿到按钮的名称用于对应
    target_class = page.get_by_text("ユウカ（体操服）").get_attribute("class")
    jpNameBtnList = page.query_selector_all(".%s" % target_class)
    for btn in jpNameBtnList:
        page.evaluate("el => el.setAttribute('jpName', '%s')" % replace_none_char(btn.text_content()), btn)
    # 切换回中文
    page.locator("svg").click()
    page.locator("#react-select-2-option-1").click()
    time.sleep(3)

    count = 0
    start_time = time.time()
    end_time = 0
    # target_list = ["日奈", "阿露", "真白", "椿"]
    target_list = ["爱莉", "枫香", "花子", "玲美", "凌音", "晴", "朱莉", "志美子", "喜美"]
    local_file_name = {
        "爱莉": "爱莉.png",
        "枫香": "风华_枫香_煮饭婆.png",
        "花子": "花子.png",
        "玲美": "玲美.png",
        "晴": "晴.png",
        "凌音": "凌音.png",
        "朱莉": "朱莉.png",
        "志美子": "志美子_图书妹.png",
        "喜美": "喜美.png",
    }
    for name in jpNameBtnList:
        # if count != 98:
        #     count = count + 1
        #     continue
        jpName = name.get_attribute("jpName")
        info = student_dict[jpName]
        # if info["cnName"] not in target_list:
        #     continue
        # 下载远端图片
        remote = query_remote_name(info["cnName"])
        path = str(remote["path"])
        png_name = path.replace("/student_rank/", "")
        name_list = png_name.replace(".png", "").split("_")
        first_name = name_list[0]
        if not test_name_exist(first_name):
            name_list.remove(first_name)
            first_name = name_list[0]
            png_name = "_".join(name_list) + ".png"
        local_path = "./image/parse/%s" % png_name
        if os.path.exists(local_path):
            count = count + 1
            print("skip: %s, %d/%d" % (first_name, count, 118))
            end_time = time.time()
            start_time = end_time
            continue
        # 从远端下载原图
        source_im = download_image("https://arona.cdn.diyigemt.com/image", path, local_path)
        # source_im = cv2.imdecode(np.fromfile(local_path, dtype=np.uint8), -1)
        # 从shaledb下载
        fetch_data_from_schaledb(info["loma"])
        name.click()
        time.sleep(2)
        # 下载拉满需要的资源图片之类的
        skill_resource_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[1]/div[2]/div[2]")
        skill_resource_btn.click()

        # 拿到资源列表的class判断是8行资源还是7行
        target_class = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[4]").get_attribute("class")
        resource_size = len(page.query_selector_all(".%s" % target_class))

        skill_resource_1 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[4]")
        skill_resource_1.screenshot(path="./image/tmp/skill_resource_1.png")
        skill_resource_2 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[5]")
        skill_resource_2.screenshot(path="./image/tmp/skill_resource_2.png")
        skill_resource_3 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[6]")
        skill_resource_3.screenshot(path="./image/tmp/skill_resource_3.png")
        skill_resource_4 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[7]")
        skill_resource_4.screenshot(path="./image/tmp/skill_resource_4.png")
        if resource_size == 8:
            skill_resource_5 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[8]")
            skill_resource_5.screenshot(path="./image/tmp/skill_resource_5.png")
            resource_1 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[9]")
            resource_1.screenshot(path="./image/tmp/resource_1.png")
            resource_2 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[10]")
            resource_2.screenshot(path="./image/tmp/resource_2.png")
            resource_3 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[11]")
            resource_3.screenshot(path="./image/tmp/resource_3.png")
        else:
            resource_1 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[8]")
            resource_1.screenshot(path="./image/tmp/resource_1.png")
            resource_2 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[9]")
            resource_2.screenshot(path="./image/tmp/resource_2.png")
            resource_3 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[10]")
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

        # 拼接技能成长材料
        skill_resource_save_path = "./image/tmp/skill_resource.png"
        base_path = "./image/tmp/"
        path_list = []
        path_list.append(base_path + "skill_resource_1.png")
        path_list.append(base_path + "skill_resource_2.png")
        path_list.append(base_path + "skill_resource_3.png")
        path_list.append(base_path + "skill_resource_4.png")
        if resource_size == 8:
            path_list.append(base_path + "skill_resource_5.png")
        path_list.append(base_path + "resource_1.png")
        path_list.append(base_path + "resource_2.png")
        path_list.append(base_path + "resource_3.png")

        concat_list(path_list, skill_resource_save_path, -3)

        # 将技能材料和装备材料拼在一起
        skill_resource_equipment = "./image/tmp/skill_resource_equipment.png"
        concat_two_im(skill_resource_save_path, base_path + "equipment_resource.png", skill_resource_equipment)

        # 将技能描述拼在一起

        concat_two_im(base_path + "ex_skill.png", base_path + "base_skill.png", base_path + "skill_desc_1.png")
        concat_two_im(base_path + "enhance_skill.png", base_path + "sub_skill.png", base_path + "skill_desc_2.png")
        concat_two_im(base_path + "skill_desc_1.png", base_path + "skill_desc_2.png", base_path + "skill_desc.png", type="vertical")

        # 把技能描述和材料拼在一起

        concat_two_im(skill_resource_equipment, base_path + "skill_desc.png", base_path + "game_db.png", type="vertical")

        # 和schaledb的拼在一起

        final_db_im = concat_two_im(base_path + "game_db.png", base_path + "schaledb.png", base_path + "final_db.png")

        # 和夜喵拼在一起
        # source_im = cv2.cvtColor(source_im, cv2.COLOR_BGR2BGRA)
        source_row, source_col, _ = source_im.shape
        final_db_row, final_db_col, _ = final_db_im.shape
        if final_db_col > source_col:
            pp = base_path + "final_db.png"
            im = Image.open(pp)
            (x, y) = im.size
            rate = source_col / final_db_col
            resize = im.resize((int(x * rate), int(y * rate)), Image.ANTIALIAS)
            resize.save(pp)
            final_db_im = cv2.imdecode(np.fromfile(pp, dtype=np.uint8), -1)
            final_db_row, final_db_col, _ = final_db_im.shape
        col = final_db_col + 10
        row = source_row + final_db_row + 10
        im = Image.new('RGBA', (col, row), color='white')
        im.save(local_path)
        im = cv2.imdecode(np.fromfile(local_path, dtype=np.uint8), -1)
        im[0: source_row, 0: source_col] = source_im
        im[source_row + 10: source_row + 10 + final_db_row, 10: final_db_col + 10] = final_db_im
        # im = cv2.cvtColor(im, cv2.COLOR_BGRA2BGR)
        cv2.imencode(".png", im)[1].tofile(local_path)
        count = count + 1
        end_time = time.time()
        print("success: %s, %d/%d, spend: %ds" % (info["cnName"], count, 118, (end_time - start_time)))
        start_time = end_time
        # 关闭信息窗口
        close_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[1]")
        close_btn.click()

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

def build_stu_loma_map_from_schaledb(playwright: Playwright):
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()
    # 从schaledb获取所有学生用于描述的姓名
    page.goto("https://lonqie.github.io/SchaleDB")

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
        # 人工替换正确名字
        if name in name_map_dict:
            name = name_map_dict[name]
        remote = query_remote_name(name)
        remote_name = str(remote["name"])
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
    final_name_dict = {}
    for item in page.query_selector_all(".card-student"):
        loma = re.sub(re_replace, r"\1", item.get_attribute("onclick"))
        jpName = item.query_selector(".card-label").query_selector(".label-text").text_content()
        jpName = replace_none_char(jpName)
        # 理论上不会发生
        if jpName in stu_loma_dict:
            print("crash: jpName: %s, loma: %s, cnName: %s" % (jpName, loma, stu_loma_dict[loma]))
            exit(-1)
        else:
            print("success: %s,%s" % (jpName, stu_loma_dict[loma]))
            final_name_dict[jpName] = {
            "cnName": stu_loma_dict[loma],
            "loma": loma
            }
    if os.path.exists(cache_file_location):
        os.remove(cache_file_location)
    with codecs.open(cache_file_location, "w", encoding="utf-8") as f:
        f.write(json.dumps(final_name_dict, ensure_ascii=False))
        f.flush()
    context.close()
    browser.close()

def concat_list(paths: list[str], save_path, margin = 0):
    im_list = list(map(lambda path: cv2.imdecode(np.fromfile(path, dtype=np.uint8), -1), paths))
    rows = reduce(lambda prv, im: im.shape[0] + prv, im_list, 0) + margin * (len(paths) - 1)
    cols = im_list[0].shape[1]
    final = Image.new('RGBA', (cols, rows), color='white')
    final.save(save_path)
    final = cv2.imdecode(np.fromfile(save_path, dtype=np.uint8), -1)
    curRows = 0
    for im in im_list:
        row, col, _ = im.shape
        final[curRows:curRows + row, 0: col] = im
        curRows = curRows + row + margin

    cv2.imencode(".png", final)[1].tofile(save_path)
    return final

def concat_two_im(path_a: str, path_b: str, path: str, type: str = 'horizen', margin = 0):
    im_a = cv2.imdecode(np.fromfile(path_a, dtype=np.uint8), -1)
    im_b = cv2.imdecode(np.fromfile(path_b, dtype=np.uint8), -1)
    row_a, col_a, _ = im_a.shape
    row_b, col_b, _ = im_b.shape
    row = 0
    col = 0
    if type == 'horizen':
        row = max(row_a, row_b)
        col = col_a + col_b + margin
    else:
        row = row_a + row_b + margin
        col = max(col_a, col_b)
    im = Image.new('RGBA', (col, row), color='white')
    im.save(path)
    im = cv2.imdecode(np.fromfile(path, dtype=np.uint8), -1)
    if type == 'horizen':
        im[0: row_a, 0: col_a] = im_a
        im[0: row_b, col_a + margin: col_a + margin + col_b] = im_b
    else:
        im[0: row_a, 0: col_a] = im_a
        im[row_a + margin: row_a + margin + row_b, 0: col_b] = im_b
    cv2.imencode(".png", im)[1].tofile(path)
    return im

def fetch_data_from_schaledb(name):
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()
    page.goto("https://lonqie.github.io/SchaleDB/?chara=%s" % name)
    page.wait_for_load_state()

    # 换成日服中文
    setting_btn = page.query_selector("#ba-navbar-regionselector")
    setting_btn.click()
    region_btn = page.query_selector("#ba-navbar-regionselector-0")
    region_btn.click()
    language_btn = page.query_selector("#ba-navbar-languageselector")
    language_btn.click()
    language_cn_btn = page.query_selector("#ba-navbar-languageselector-cn")
    language_cn_btn.click()

    # 删掉背景
    page.evaluate("el => el.remove()", page.query_selector("#ba-background"))

    # 立绘
    page.query_selector("#ba-student-img").screenshot(path="./image/tmp/stu.png")

    weapon_btn = page.query_selector("#ba-student-tab-weapon")
    weapon_btn.click()
    # 拉满
    progress = page.query_selector("#ba-weaponpreview-levelrange")
    page.evaluate("input => input.value = '50'", progress)
    page.evaluate("input => input.oninput(input)", progress)
    weapon_name = page.query_selector("//*[@id='ba-student-page-weapon']/div[1]")
    weapon_name.screenshot(path="./image/tmp/weapon_name.png")
    weapon_img = page.query_selector("//*[@id='ba-student-page-weapon']/div[2]")
    weapon_img.screenshot(path="./image/tmp/weapon_img.png")

    base_info_btn = page.query_selector("#ba-student-tab-profile")
    base_info_btn.click()

    name_card = page.query_selector("//*[@id='ba-student-page-profile']/div[1]")
    name_card.screenshot(path="./image/tmp/name_card.png")
    base_card = page.query_selector("//*[@id='ba-student-page-profile']/table/tbody")
    base_card.screenshot(path="./image/tmp/base_card.png")
    gift_banner = page.query_selector("//*[@id='ba-student-page-profile']/div[4]/h5")
    gift_banner.screenshot(path="./image/tmp/gift_banner.png")
    gift = page.query_selector("//*[@id='ba-student-favoured-items']")
    gift.screenshot(path="./image/tmp/gift.png")
    furniture_banner = page.query_selector("//*[@id='ba-student-page-profile']/div[6]/h5")
    furniture_banner.screenshot(path="./image/tmp/furniture_banner.png")
    furniture = page.query_selector("//*[@id='ba-student-favoured-furniture']")
    furniture.screenshot(path="./image/tmp/furniture.png")

    # 拼到一起
    save_path = "./image/tmp/schaledb.png"
    base_path = "./image/tmp/"
    path_list = []
    path_list.append(base_path + "name_card.png")
    path_list.append(base_path + "base_card.png")
    path_list.append(base_path + "weapon_name.png")
    path_list.append(base_path + "weapon_img.png")
    path_list.append(base_path + "gift_banner.png")
    path_list.append(base_path + "gift.png")
    path_list.append(base_path + "furniture_banner.png")
    path_list.append(base_path + "furniture.png")

    concat_list(path_list, save_path)
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
    if len(result["data"]) > 1:
        return query_remote_name(result["data"][0]["name"])
    elif len(result["data"]) == 0:
        return {"name": name}
    return result["data"][0]

def replace_none_char(name: str) -> str:
    return name.replace("（", "(").replace("）", ")")

with sync_playwright() as playwright:
    run(playwright)