import os
import re
import cv2
import math
import time
import json
import codecs
import threading
import numpy as np
from PIL import Image
from config import cache_file_location, cn_translation_location
from playwright.sync_api import Playwright, sync_playwright, Route, Request
from fetch_student_info_from_ba_game_db import concat_list, concat_two_im, download_image, fetch_data_from_game_db, fetch_data_from_schaledb, fetch_skill_data_from_schaledb, path_with_thread_id, query_remote_name, replace_none_char, test_name_exist

from tools import confirm_action, draw_image_source
# 要生成的目标 日文名
target = []

# game-db数据更新缓慢, 使用override的数据
use_game_db_override = False
game_db_override_script = "https://ba.game-db.tw/static/main.01855ff23e40cfbf5e17.js"

sources_map = {
    "schaledb": "部分学生信息,技能数据来源: http://schale.gg/",
    "gamedb": "部分技能翻译,技能数据来源: https://ba.game-db.tw/",
    "gamekee": "部分技能翻译,角色翻译来源: https://ba.gamekee.com/ && https://wiki.biligame.com/ && https://kivo.wiki/",
}

lock = threading.Lock()
max_thread = min(os.cpu_count(), 4)

# 加载dict
cache_dict = {}
with codecs.open(cache_file_location, "r", encoding="utf-8") as f:
    cache_dict = json.loads(f.read())

def game_db_content_override(route: Route, req: Request):
    route.fulfill(path="playwright/fake.js")

def run(playwright: Playwright, arr: list[str], thread_id: int):
    with codecs.open("./config/local_file_map.json", "r", encoding="utf-8") as f:
        local_file_path = json.load(f)
    browser = playwright.chromium.launch(
        proxy={"server":"http://127.0.0.1:12350"},
        headless=True,
        chromium_sandbox=False,
        args=[r"--disk-cache-dir=D:\tmp\playwright"],
        slow_mo=100
        )
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    context.set_extra_http_headers({"Cache-Control": "max-age=3600"})
    page = context.new_page()
    if (use_game_db_override):
        page.add_init_script(path="playwright/init.js")
        page.route(game_db_override_script, game_db_content_override)
        page.route("https://ba.game-db.tw/images/items/equipment_icon_watch_tier9.png", lambda r, _ : r.fulfill(path="playwright/im/equipment_icon_watch_tier9.webp"))
        page.route("https://ba.game-db.tw/images/items/equipment_icon_necklace_tier9.png", lambda r, _ : r.fulfill(path="playwright/im/equipment_icon_necklace_tier9.webp"))
        page.route("https://ba.game-db.tw/images/items/equipment_icon_watch_tier9_piece.png", lambda r, _ : r.fulfill(path="playwright/im/equipment_icon_watch_tier9_piece.webp"))
        page.route("https://ba.game-db.tw/images/items/equipment_icon_necklace_tier9_piece.png", lambda r, _ : r.fulfill(path="playwright/im/equipment_icon_necklace_tier9_piece.webp"))
    # 拿到成长资源截图
    page.goto("https://ba.game-db.tw/")
    page.wait_for_load_state()
    page.locator("svg").first.click()
    page.locator("#react-select-2-option-0").click()
    page.get_by_text("一覧").click()

    target_class = page.get_by_text("ユウカ（体操服）").get_attribute("class")
    jpNameBtnList = page.query_selector_all(".%s" % target_class)
    print("build btn mapping...")
    for btn in jpNameBtnList:
        page.evaluate("el => el.setAttribute('jpName', '%s')" % replace_none_char(btn.text_content()), btn)
    print("build complete")

    # 加载翻译
    cn_translate_dict = {}
    with codecs.open(cn_translation_location, "r", encoding="utf-8") as f:
        cn_translate_dict = json.loads(f.read())

    count = 0
    for jpName in arr:
        if jpName not in cache_dict:
            count = count + 1
            print("%s not found in dict" % jpName)
            continue
        info = cache_dict[jpName]
        cnName = info["cnName"]
        loma = info["loma"]
        offset = info["offset"] if "offset" in info else 0
        print("start: %s" % cnName)
        # 找到页面上的对应按钮
        btnFilterList = list(filter(lambda btn: btn.get_attribute("jpName") == jpName, jpNameBtnList))
        if len(btnFilterList) == 0:
            count = count + 1
            print("%s not found in btn" % jpName)
            continue
        
        # 切换回中文
        page.locator("svg").first.click()
        page.locator("#react-select-2-option-1").click()
        time.sleep(3)
        start_time = time.time()
        end_time = 0
        # 开始下载远端文件
        remote = query_remote_name(info["cnName"])
        path = str(remote["content"])
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
            if not os.path.exists(local_path):
                count = count + 1
                print("local file not found, start download from remote: %s" % cnName)
                source_im = download_image("https://arona.cdn.diyigemt.com/image", path, local_path)
            else:
                source_im = cv2.imdecode(np.fromfile(local_path, dtype=np.uint8), -1)
        else:
            source_im = download_image("https://arona.cdn.diyigemt.com/image", path, local_path)

        # 获取gamekee的中文翻译
        if info["gamekee"] != "":
            cn_info = get_cn_info_from_gamekee(playwright, info["gamekee"])
        else:
            cn_info = {
                "ex_name": ""
            }
        
        # 有时候gamekee太恶心了拿不到, 手动填上
        if jpName in cn_translate_dict:
            cn_info.update(cn_translate_dict[jpName])

        # 从shaledb下载 如果有爱用品信息 顺便拿到爱用品
        # 专武和学生介绍
        cn_info = fetch_data_from_schaledb(playwright, loma, cn_info, thread_id)

        # 从gamedb下载
        btnFilterList[offset].click()
        time.sleep(2)
        # 中日切换判断是否有翻译
        try:
            cn_skill = page.query_selector('//*[@id="skill1"]/div/div[1]').text_content()
        except Exception as e:
            cn_skill = ""
        finally:
            # 关闭信息窗口
            close_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[1]")
            if close_btn != None:
                close_btn.click()
        # 切换回日文
        page.locator("svg").first.click()
        page.locator("#react-select-2-option-0").click()
        btnFilterList[offset].click()
        time.sleep(2)
        try:
            jp_skill = page.query_selector('//*[@id="skill1"]/div/div[1]').text_content()
            # 关闭信息窗口
            close_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[1]")
            close_btn.click()
        except Exception as e:
            jp_skill = "-1"
        try:
            # 切换回中文
            page.locator("svg").first.click()
            page.locator("#react-select-2-option-1").click()
            time.sleep(2)
            btnFilterList[offset].click()
            time.sleep(2)
        except Exception as e:
            pass
        base_path = "./image/tmp/"
        # 下载拉满需要的资源图片之类的
        skill_resource_equipment_path = fetch_data_from_game_db(page, cn_info, cn_skill == jp_skill and (("ex_name" in cn_info) and cn_info["ex_name"] != ""), thread_id=thread_id)

        # 从schaledb获取技能描述图片
        skill_path = fetch_skill_data_from_schaledb(playwright, loma, thread_id)
        concat_two_im(skill_resource_equipment_path, skill_path, skill_resource_equipment_path, type="vertical", reshape=True, reshapeType="l")

        # 和schaledb的拼在一起

        final_db_pah = path_with_thread_id("./image/tmp/final_db.png", thread_id)

        final_db_im = concat_two_im(skill_resource_equipment_path, path_with_thread_id("./image/tmp/schaledb.png", thread_id), final_db_pah)

        # 和夜喵拼在一起 

        source_row, source_col, dimension = source_im.shape
        if dimension == 3:
            source_im = cv2.cvtColor(source_im, cv2.COLOR_BGR2BGRA)

        final_db_row, final_db_col, _ = final_db_im.shape
        if final_db_col > source_col:
            im = Image.open(final_db_pah)
            (x, y) = im.size
            rate = source_col / final_db_col
            resize = im.resize((int(x * rate), int(y * rate)), Image.Resampling.LANCZOS)
            resize.save(final_db_pah)
            final_db_im = cv2.imdecode(np.fromfile(final_db_pah, dtype=np.uint8), -1)
            final_db_row, final_db_col, _ = final_db_im.shape
        col = final_db_col + 10
        row = source_row + final_db_row + 40
        im = Image.new('RGBA', (col, row), color='white')
        im.save(local_path)
        im = cv2.imdecode(np.fromfile(local_path, dtype=np.uint8), -1)
        im[0: source_row, 0: source_col] = source_im
        im[source_row + 10: source_row + 10 + final_db_row, 10: final_db_col + 10] = final_db_im
        # im = cv2.cvtColor(im, cv2.COLOR_BGRA2BGR)
        cv2.imencode(".png", im)[1].tofile(local_path)
        # 加上出处
        source_offset = -1
        for key in sources_map:
            im = draw_image_source(local_path, sources_map[key], offset=source_offset)
            cv2.imencode(".png", im)[1].tofile(local_path)
            source_offset = source_offset + 1
        count = count + 1
        end_time = time.time()

        # loacal
        print("success: %s, %d/%d, spend: %ds" % (cnName, count, len(arr), (end_time - start_time)))
        
        start_time = end_time
        # 关闭信息窗口
        close_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[1]")
        close_btn.click()

def get_cn_info_from_gamekee(playwright: Playwright, path: str):
    browser = playwright.chromium.launch(
    headless=True,
    chromium_sandbox=False,
    args=[r"--disk-cache-dir=D:\tmp\playwright"],
    slow_mo=100
    )
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    context.set_extra_http_headers({"Cache-Control": "max-age=3600"})
    page = context.new_page()
    try:
        page.goto(path)
    except Exception as e:
        pass
    time.sleep(2)
    page.eval_on_selector_all(".dailog-data-wrapper", "nodes => nodes.forEach(el => el.remove())")
    page.eval_on_selector_all(".back-item", "nodes => nodes.forEach(el => el.remove())")
    time.sleep(2)
    skill_bounds = re.compile("[\d.]+[%％秒]?[∼～~][\d.]+[%％秒]?")

    info = {}
    # prefix = [
    #     '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/span/span/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div/span/span/div/div/span/div/span/div/div/span/div/span/div/div/div/span/div/span/div/div/div[3]/span/div/span/div/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div[2]/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div/span/div[1]/span/div/span/div/div/span/div/span/div/div/div/span/div/span/div/div/div[3]/span/div/span/div/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/div/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div[1]/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div[1]/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/div/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div[1]/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
    #     '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
    # ]
    # ex_name, prefix_index = get_content(page, list(map(lambda x: x + 'tr[1]', prefix)), True)
    # ex_desc, prefix_index = get_content(page, list(map(lambda x: x + 'tr[2]/td[2]', prefix)))
    
    # ex_desc = skill_bounds.sub("$value", ex_desc)
    # if ex_desc.find("COST：") != -1:
    #     ex_desc = ex_desc[0:ex_desc.find("COST：")]

    # bs_name, prefix_index = get_content(page, list(map(lambda x: x + 'tr[5]', prefix)), True)
    # bs_desc, prefix_index = get_content(page, list(map(lambda x: x + 'tr[6]/td[2]', prefix)))
    # bs_desc = skill_bounds.sub("$value", bs_desc)

    # es_name, prefix_index = get_content(page, list(map(lambda x: x + 'tr[9]', prefix)), True)
    # es_desc, prefix_index = get_content(page, list(map(lambda x: x + 'tr[10]/td[2]', prefix)))
    # es_desc = skill_bounds.sub("$value", es_desc)

    # ss_name, prefix_index = get_content(page, list(map(lambda x: x + 'tr[13]', prefix)), True)
    # ss_desc, prefix_index = get_content(page, list(map(lambda x: x + 'tr[14]/td[2]', prefix)))
    # ss_desc = skill_bounds.sub("$value", ss_desc)

    wp_prefix = [
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/span/span/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div/span/span/div/div/span/div/span/div/div/span/div/span/div/div/div/span/div/span/div/div/div[3]/span/div/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div[2]/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[3]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div/span/div[1]/span/div/span/div/div/span/div/span/div/div/div/span/div/span/div/div/div[3]/span/div/span/div/div[1]/div[2]/div[2]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[3]/div[1]/div/div/table/tbody/tr[2]/td/div/span/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[3]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/span/span/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div[1]/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[3]/div[1]/div/div/table/tbody/tr[2]/td/div/span/',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div[1]/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[2]/td/div/',
    ]
    wp_name, prefix_index = get_content(page, list(map(lambda x: x + 'div[1]', wp_prefix)))
    
    wp_desc_1, prefix_index = get_content(page, list(map(lambda x: x + 'div[2]', wp_prefix)))
    wp_desc_2, prefix_index = get_content(page, list(map(lambda x: x + 'div[3]', wp_prefix)))

    wp_skill, prefix_index = get_content(page,
    [
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/span/span/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[16]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[15]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div/span/span/div/div/span/div/span/div/div/span/div/span/div/div/div/span/div/span/div/div/div[3]/span/div/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[15]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div[2]/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[3]/div[1]/div/div/table/tbody/tr[15]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div/span/div[1]/span/div/span/div/div/span/div/span/div/div/div/span/div/span/div/div/div[3]/span/div/span/div/div[1]/div[2]/div[2]/div/div/table/tbody/tr[15]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[3]/div[1]/div/div/table/tbody/tr[16]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[3]/div[1]/div/div/table/tbody/tr[15]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[16]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[16]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[16]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[15]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div/div[2]/div/div/table/tbody/tr[16]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[16]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[15]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div[1]/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[15]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[16]/td',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[15]/td',
    '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[15]/td',
    '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div[1]/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[16]/td',
    '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[16]/td',
    '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[15]/td',
    '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[3]/div[1]/div/div/table/tbody/tr[15]/td',
    '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[15]/td',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[16]/td',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div[1]/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[16]/td',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[15]/td',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[1]/div/div/table/tbody/tr[16]/td',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/div/div[1]/div[2]/div/div[2]/div/div/table/tbody/tr[16]/td',
    ]
    )
    wp_skill = skill_bounds.sub("$value", wp_skill)

    desc_prefix = [
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/span/span/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div/span/span/div/div/span/div/span/div/div/span/div/span/div/div/div/span/div/span/div/div/div[3]/span/div/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div[2]/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[3]/div[5]/div/div/table/tbody',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div/span/div[1]/span/div/span/div/div/span/div/span/div/div/div/span/div/span/div/div/div[3]/span/div/span/div/div[1]/div[2]/div[5]/div[1]/div/div/table/tbody',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[3]/div[5]/div/div/table/tbody',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div[1]/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div[1]/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    '//*[@id="wiki-body"]/div/div/div[3]/div[1]/div/div[2]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div[1]/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/div/div[1]/div[2]/div/div[5]/div/div/table/tbody/',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    '//*[@id="wiki-body"]/div/div/div[3]/div/div/div[1]/div[1]/div[4]/div/span/span/div/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/div[1]/div[2]/div[2]/div[4]/div/div/table/tbody/',
    ]

    
    hobby_extend_list = []
    hobby, prefix_index = get_content(page, list(map(lambda x: x + '/tr[6]/td[2]', desc_prefix)) + hobby_extend_list)
    
    if page.query_selector(desc_prefix[prefix_index] + '/tr[11]') != None:
        hobby, prefix_index = get_content(page, list(map(lambda x: x + '/tr[7]/td[2]', desc_prefix)) + hobby_extend_list)
        desc_suffix = '/tr[11]/td[2]'
    elif page.query_selector(desc_prefix[prefix_index] + '/tr[10]') != None:
        hobby, prefix_index = get_content(page, list(map(lambda x: x + '/tr[6]/td[2]', desc_prefix)) + hobby_extend_list)
        desc_suffix = '/tr[10]/td[2]'
    else:
        # 解决多出来的繁中翻译列
        desc_suffix = '/tr[9]/td[2]'

    desc_extend_list = []
    desc, prefix_index = get_content(page, list(map(lambda x: x + desc_suffix, desc_prefix)) + desc_extend_list)
    # 处理换行
    desc_list = desc.split("。")
    desc = desc_list[0] + "\n" + "".join(desc_list[1:])

    # info["ex_name"] = ex_name
    # info["ex_desc"] = ex_desc
    # info["bs_name"] = bs_name
    # info["bs_desc"] = bs_desc
    # info["es_name"] = es_name
    # info["es_desc"] = es_desc
    # info["ss_name"] = ss_name
    # info["ss_desc"] = ss_desc
    info["wp_name"] = wp_name
    info["wp_desc_1"] = wp_desc_1
    info["wp_desc_2"] = wp_desc_2
    info["wp_skill"] = wp_skill
    info["hobby"] = hobby
    info["desc"] = desc

    context.close()
    browser.close()
    return info

def get_content(page, xPaths: str, isSingleLine = False) -> str:
    index = 0
    for xPath in xPaths:
        index = index + 1
        el = page.query_selector(xPath)
        if el != None:
            content = el.text_content().replace(" ", "")
            if isSingleLine:
                content = content.replace("\n", "")
            else:
                content = content.replace("\n", "\\n")
            if content.replace("\\n", "") == '':
                continue
            return content, index - 1
    # print(xPaths)
    return "", -1

def thread_run(arr, id):
   with sync_playwright() as playwright:
        run(playwright, arr, id) 

def split_arr(arr, size):
    size = math.ceil(len(arr) / size)
    s = []
    for i in range(0, int(len(arr)) + 1, size):
        c = arr[i:i + size]
        if c != []:
            s.append(c)
    return s

if __name__ == "__main__":
    if len(target) == 0:
        for file in os.listdir("./image/parse/"):
            if not file.endswith(".png"):
                continue
            file_name = file.replace(".png", "")
            for key in cache_dict:
                raw = cache_dict[key]
                if raw["cnName"] == file_name:
                    target.append(key)
    
    splited_arr = split_arr(target, max_thread)
    if len(splited_arr) == 0:
        threads = [threading.Thread(target=thread_run, args=([],))]
    else:
        threads = [threading.Thread(target=thread_run, args=(arr,index,)) for index, arr in enumerate(splited_arr)]
    if use_game_db_override:
        if not confirm_action("内容覆盖已打开, 确保内容正确?"):
            use_game_db_override = False
    print("start with %d threads" % len(threads))
    for t in threads:
        t.start()
    for t in threads:
        t.join()