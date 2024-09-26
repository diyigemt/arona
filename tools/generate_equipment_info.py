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
equipment_max_tiger = 7    # 装备等级
equipment_max_type = 9     # 装备种类
equipment_id_template = "200$type00$tiger"  # 装备id模板


def run(playwright: Playwright):
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()
    page.goto("https://lonqie.github.io/SchaleDB/")
    page.wait_for_load_state()

    # 关闭change-log窗口
    # model = page.query_selector_all(".show")
    # if len(model) != 0:
    #     close_btn = page.query_selector(".btn-close-white")
    #     if close_btn != None:
    #         close_btn.click()
    # 拿到装备截图
    final_list = []
    for type in range(1, equipment_max_type + 1):
        eq_list = []
        for tiger in range(equipment_max_tiger):
            id = equipment_id_template.replace("$type", str(type)).replace("$tiger", str(tiger))
            page.goto("https://lonqie.github.io/SchaleDB/?item=%s" % id)
            time.sleep(2)

            # 名字
            equipment_desc = page.query_selector('//*[@id="ba-item-details-container"]/div[1]/div')
            equipment_desc.screenshot(path="./image/tmp/equipment_desc.png", type="png")
            # 属性
            equipment_skill = page.query_selector('//*[@id="ba-item-details-container"]/div[2]/div[1]')
            equipment_skill.screenshot(path="./image/tmp/equipment_skill.png", type="png")

            # 拼接
            save_path = "./image/parse/%s.png" % id
            path_list = []
            path_list.append("./image/tmp/equipment_desc.png")
            path_list.append("./image/tmp/equipment_skill.png")

            concat_list(path_list, save_path)
            eq_list.append(save_path)
        type_name = page.query_selector('//*[@id="ba-item-type"]').text_content()
        type_save_path = "./image/parse/%s.png" % type_name
        concat_list(eq_list, type_save_path)
        final_list.append(type_save_path)
        eq_list.clear()
    last_path = final_list[0]
    for i in range(1, len(final_list)):
        current_path = final_list[i]
        save_path = "./image/parse/eq_tmp_%d.png" % i
        concat_two_im(last_path, current_path, save_path)
        last_path = save_path
if __name__ == "__main__":
    with sync_playwright() as playwright:
        run(playwright)