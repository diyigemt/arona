import cv2
import os
import time
from playwright.sync_api import Playwright, sync_playwright
from PIL import Image
import codecs
import json
from config import cache_file_location
import numpy as np
from fetch_student_info_from_ba_game_db import concat_list, concat_two_im, download_image, fetch_data_from_game_db, fetch_data_from_schaledb, query_remote_name, replace_none_char, test_name_exist
import re
# 要生成的目标 日文名
target = ["ミユ"]
# 如果本地有图片
local_file_path = {
    "ミユ": "美游.png",
}

def run(playwright: Playwright):
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()

    # 拿到成长资源截图
    page.goto("https://ba.game-db.tw/")
    page.locator("svg").click()
    page.locator("#react-select-2-option-0").click()
    page.get_by_text("一覧表").click()

    target_class = page.get_by_text("ユウカ（体操服）").get_attribute("class")
    jpNameBtnList = page.query_selector_all(".%s" % target_class)
    print("build btn mapping...")
    for btn in jpNameBtnList:
        page.evaluate("el => el.setAttribute('jpName', '%s')" % replace_none_char(btn.text_content()), btn)
    print("build complete")

    if len(target) == 0:
        exit(0)
    # 加载dict
    cache_dict = {}
    with codecs.open(cache_file_location, "r", encoding="utf-8") as f:
        cache_dict = json.loads(f.read())
    count = 0
    for jpName in target:
        if jpName not in cache_dict:
            count = count + 1
            print("%s not found in dict" % jpName)
            continue
        info = cache_dict[jpName]
        cnName = info["cnName"]
        loma = info["loma"]
        print("start: %s" % cnName)
        # 找到页面上的对应按钮
        btnFilterList = list(filter(lambda btn: btn.get_attribute("jpName") == jpName, jpNameBtnList))
        if len(btnFilterList) != 1:
            count = count + 1
            print("%s not found in btn" % jpName)
            continue
        
        # 切换回中文
        page.locator("svg").click()
        page.locator("#react-select-2-option-1").click()
        time.sleep(3)
        count = 0
        start_time = time.time()
        end_time = 0
        # 开始下载远端文件
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

        if os.path.exists(local_path) and jpName not in local_file_path:
            count = count + 1
            print("skip: %s, %d/%d" % (cnName, count, 118))
            end_time = time.time()
            start_time = end_time
            continue
        # 如果本地有就不从远端下载了
        source_im = None
        if jpName in local_file_path:
            local_path = "./image/parse/%s" % local_file_path[jpName]
            if local_path.find(".png") == -1:
                local_path = local_path + ".png"
            source_im = cv2.imdecode(np.fromfile(local_path, dtype=np.uint8), -1)
        else:
            source_im = download_image("https://arona.cdn.diyigemt.com/image", path, local_path)

        # 获取gamekee的中文翻译

        cn_info = get_cn_info_from_gamekee(playwright, info["gamekee"])

        # 从shaledb下载
        fetch_data_from_schaledb(playwright, loma, cn_info)

        # 从gamedb下载
        btnFilterList[0].click()
        time.sleep(2)

        base_path = "./image/tmp/"
        # 下载拉满需要的资源图片之类的
        fetch_data_from_game_db(page, cn_info, base_path)

        # 和schaledb的拼在一起

        final_db_im = concat_two_im(base_path + "game_db.png", base_path + "schaledb.png", base_path + "final_db.png")

        # 和夜喵拼在一起

        source_row, source_col, dimension = source_im.shape
        if dimension == 3:
            source_im = cv2.cvtColor(source_im, cv2.COLOR_BGR2BGRA)

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

        # loacal
        print("success: %s, %d/%d, spend: %ds" % (cnName, count, len(target), (end_time - start_time)))
        
        start_time = end_time
        # 关闭信息窗口
        close_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[1]")
        close_btn.click()

def get_cn_info_from_gamekee(playwright: Playwright, path: str):
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()

    page.goto(path)
    time.sleep(2)
    page.eval_on_selector_all(".dailog-data-wrapper", "nodes => nodes.forEach(el => el.remove())")
    time.sleep(2)
    skill_bounds = re.compile("\d+%?~\d+%?")

    info = {}
    ex_name = get_content(page,'//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/tr[1]/td/div/span')
    ex_desc = get_content(page,'//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/tr[2]/td[2]')
    ex_desc = skill_bounds.sub("$value", ex_desc)
    if ex_desc.find("COST") != -1:
        ex_desc = ex_desc[0:ex_desc.find("COST")]

    ns_name = get_content(page,'//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/tr[5]/td/div/span')
    ns_desc = get_content(page,'//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/tr[6]/td[2]')
    ns_desc = skill_bounds.sub("$value", ns_desc)

    bs_name = get_content(page,'//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/tr[9]/td/div/span')
    bs_desc = get_content(page,'//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/tr[10]/td[2]')
    bs_desc = skill_bounds.sub("$value", bs_desc)

    ss_name = get_content(page,'//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/tr[13]/td/div/span')
    ss_desc = get_content(page,'//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/tr[14]/td[2]')
    ss_desc = skill_bounds.sub("$value", ss_desc)

    wp_name = get_content(page,'//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/div[1]/span')
    
    wp_desc_1_el = page.query_selector('//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div/span/span/span[1]/span/div/span/span[2]/span/span/span/span/span/div/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[3]/span/span/div/span/span[2]/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/div[2]')
    wp_desc_2_el = page.query_selector('//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div/span/span/span[1]/span/div/span/span[2]/span/span/span/span/span/div/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[3]/span/span/div/span/span[2]/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/div[3]')
    if wp_desc_1_el == None:
        wp_desc_1_el = page.query_selector('//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/div[2]')
        wp_desc_2_el = page.query_selector('//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/div[3]')

    wp_desc_1 = wp_desc_1_el.text_content().replace(".", "").replace("\n", "")
    wp_desc_2 = wp_desc_2_el.text_content().replace(".", "").replace("\n", "")
    
    wp_skill = get_content(page,'//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[1]/span/span/div/span/span[2]/span/span/span/span/span/div/div[1]/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[15]/td')
    wp_skill = skill_bounds.sub("$value", wp_skill)

    hobby = get_content(page,'//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/tr[6]/td[2]/div')
    # 处理换行
    desc = get_content(page,'//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/tr[9]/td[2]')
    desc_list = desc.split("。")
    desc = desc_list[0] + "\n" + "".join(desc_list[1:])

    info["ex_name"] = ex_name
    info["ex_desc"] = ex_desc
    info["ns_name"] = ns_name
    info["ns_desc"] = ns_desc
    info["bs_name"] = bs_name
    info["bs_desc"] = bs_desc
    info["ss_name"] = ss_name
    info["ss_desc"] = ss_desc
    info["wp_name"] = wp_name
    info["wp_desc_1"] = wp_desc_1
    info["wp_desc_2"] = wp_desc_2
    info["wp_skill"] = wp_skill
    info["hobby"] = hobby
    info["desc"] = desc

    context.close()
    browser.close()
    return info

def get_content(page, xPath: str) -> str:
    content = page.query_selector(xPath).text_content()
    return content.replace(".", "").replace("\n", "").replace(" ", "")
if __name__ == "__main__":
    with sync_playwright() as playwright:
        run(playwright)