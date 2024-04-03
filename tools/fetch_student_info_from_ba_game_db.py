import json
import re
from urllib.parse import quote
import requests
from playwright.sync_api import Playwright, sync_playwright, Page
import urllib
import urllib.request
from PIL import ImageDraw, ImageFont, Image
import cv2
import numpy as np
import time
import codecs
from functools import reduce
from config import cache_file_location, name_map_dict_file_location
headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36"}
font_size = 28
fnt = ImageFont.truetype('C:\Windows\Fonts\msyh.ttc', font_size)
def concat_list(paths: list[str], save_path, margin = 0, reshape = False):
    im_list = list(map(lambda path: cv2.imdecode(np.fromfile(path, dtype=np.uint8), -1), paths))
    cols = im_list[0].shape[1]
    if reshape:
        col_list = list(map(lambda im: im.shape[1], im_list))
        cols = median(col_list)
        final_im_list = []
        for im in im_list:
            row, col, _ = im.shape
            rate = cols / col
            final_im_list.append(cv2.resize(im, (int(col*rate), int(row*rate))))
        im_list = final_im_list
    rows = reduce(lambda prv, im: im.shape[0] + prv, im_list, 0) + margin * (len(paths) - 1)

    final = Image.new('RGBA', (int(cols), int(rows)), color='white')
    final.save(save_path)
    final = cv2.imdecode(np.fromfile(save_path, dtype=np.uint8), -1)
    curRows = 0
    for im in im_list:
        row, col, dimension = im.shape
        if dimension == 3:
            im = im = cv2.cvtColor(im, cv2.COLOR_BGR2BGRA)
        final[curRows:curRows + row, 0: col] = im
        curRows = curRows + row + margin

    cv2.imencode(".png", final)[1].tofile(save_path)
    return final

# 获取数组中位数
def median(data):
    data.sort()
    half = len(data) // 2
    return (data[half] + data[~half])/2

def concat_two_im(path_a: str, path_b: str, path: str, type: str = 'horizen', margin = 0, reshape = False, reshapeType = "a"):
    """
    reshapeType
    l: 左边
    r: 右边
    a: 均值(默认)
    """
    im_a = cv2.imdecode(np.fromfile(path_a, dtype=np.uint8), -1)
    im_b = cv2.imdecode(np.fromfile(path_b, dtype=np.uint8), -1)
    row_a, col_a, _ = im_a.shape
    row_b, col_b, _ = im_b.shape
    row = 0
    col = 0
    isHorzen = type == 'horizen'
    if reshape:
        if isHorzen:
            if reshapeType == "a":
                avg_row = int((row_a + row_b) / 2)
            elif reshapeType == "l":
                avg_row = row_a
            else:
                avg_row = row_b
            rate_a = avg_row / row_a
            rate_b = avg_row / row_b
        else:
            if reshapeType == "a":
                avg_col = int((row_a + row_b) / 2)
            elif reshapeType == "l":
                avg_col = col_a
            else:
                avg_col = col_b
            rate_a = avg_col / col_a
            rate_b = avg_col / col_b
        im_a = cv2.resize(im_a, (int(col_a*rate_a), int(row_a*rate_a)))
        row_a, col_a, _ = im_a.shape
        im_b = cv2.resize(im_b, (int(col_b*rate_b), int(row_b*rate_b)))
        row_b, col_b, _ = im_b.shape
    if isHorzen:
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

def path_with_thread_id(base: str, thread_id: int):
    return "%s-%d.png" % (base, thread_id)

def fetch_data_from_schaledb(pl: Playwright, name, dict, thread_id: int):
    browser = pl.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()
    page.goto("https://schale.gg/?chara=%s" % name)
    page.wait_for_load_state()

    # 关闭change-log窗口
    model = page.query_selector("#modal-changelog")
    if model != None:
        close_btn = model.query_selector(".btn-close")
        if close_btn != None:
            try:
                close_btn.click()
            except Exception as _:
                pass
    setting_btn = page.query_selector("#ba-navbar-settings")
    setting_btn.click()
    # 换成日服
    region_btn = page.query_selector("#ba-navbar-regionselector")
    region_btn.click()
    region_btn_jp = page.query_selector("#ba-navbar-regionselector-0")
    region_btn_jp.click()
    time.sleep(2)
    # 中文与日语切换确定是否有中文翻译, 如果有就不采用gamekee的机翻
    language_btn = page.query_selector("#ba-navbar-languageselector")
    language_btn.click()
    language_jp_btn = page.query_selector("#ba-navbar-languageselector-jp")
    language_jp_btn.click()
    time.sleep(2)
    setting_btn.click()
    
    base_info_btn = page.query_selector("#ba-student-tab-profile")
    base_info_btn.click()

    jp_hobby = page.query_selector('//*[@id="ba-student-profile-hobbies"]').text_content()

    setting_btn.click()
    language_btn = page.query_selector("#ba-navbar-languageselector")
    language_btn.click()
    # 切换成民译
    language_zh_btn = page.query_selector("#ba-navbar-languageselector-zh")
    language_zh_btn.click()
    time.sleep(2)
    setting_btn.click()

    cn_hobby = page.query_selector('//*[@id="ba-student-profile-hobbies"]').text_content()

    # gamekee就是一坨答辩
    is_no_translate = jp_hobby == cn_hobby and (dict["ex_name"] != "" or dict["desc"] != "")

    # 删掉背景
    page.evaluate("el => el.remove()", page.query_selector("#ba-background"))

    # 立绘
    page.query_selector("#ba-student-img").screenshot(path=path_with_thread_id("./image/tmp/stu.png", thread_id))

    weapon_btn = page.query_selector("#ba-student-tab-weapon")
    weapon_btn.click()
    time.sleep(2)
    # 拉满
    progress = page.query_selector("#ba-weaponpreview-levelrange")
    page.evaluate("input => input.value = '50'", progress)
    page.evaluate("input => input.oninput(input)", progress)

    # 如果没有翻译,使用gamekee替换专武名称和描述
    if is_no_translate:
        page.eval_on_selector('//*[@id="ba-student-weapon-name"]', "node => node.innerText = '%s'" % dict["wp_name"])
        page.eval_on_selector('//*[@id="ba-weapon-description"]', "node => node.innerText = '%s'" % (dict["wp_desc_1"] + "\\n" + dict["wp_desc_2"]))

    weapon_name = page.query_selector("//*[@id='ba-student-page-weapon']/div[1]")
    weapon_name.screenshot(path=path_with_thread_id("./image/tmp/weapon_name.png", thread_id))
    
    weapon_img = page.query_selector("//*[@id='ba-student-page-weapon']/div[2]")
    weapon_img.screenshot(path=path_with_thread_id("./image/tmp/weapon_img.png", thread_id))

    base_info_btn = page.query_selector("#ba-student-tab-profile")
    base_info_btn.click()
    time.sleep(2)

    # 如果没有翻译,使用gamekee替换介绍名称
    if is_no_translate:
        page.eval_on_selector('//*[@id="ba-student-profile-hobbies"]', "node => node.innerText = '%s'" % dict["hobby"])
        desc = page.eval_on_selector('//*[@id="ba-student-profile-text"]', "node => node.innerHTML")
        desc_i = str(desc).find("<i class")
        if desc_i != -1:
            desc = dict["desc"] + "\\n" + str(desc)[desc_i:]
        else:
            desc = dict["desc"]
        page.eval_on_selector('//*[@id="ba-student-profile-text"]', "node => node.innerHTML = '%s'" % desc.replace("\n", "\\n"))

    name_card = page.query_selector("//*[@id='ba-student-page-profile']/div[1]")
    name_card.screenshot(path=path_with_thread_id("./image/tmp/name_card.png", thread_id))
    base_card = page.query_selector("//*[@id='ba-student-page-profile']/table/tbody")
    base_card.screenshot(path=path_with_thread_id("./image/tmp/base_card.png", thread_id))
    live2d_bannder = page.query_selector("//*[@id='ba-student-page-profile']/div[2]/h5")
    live2d_bannder.screenshot(path=path_with_thread_id("./image/tmp/live2d_banner.png", thread_id))
    live2d = page.query_selector("//*[@id='ba-student-page-profile']/div[3]/div")
    live2d.screenshot(path=path_with_thread_id("./image/tmp/live2d.png", thread_id))
    gift_banner = page.query_selector("//*[@id='ba-student-page-profile']/div[4]/h5")
    gift_banner.screenshot(path=path_with_thread_id("./image/tmp/gift_banner.png", thread_id))
    gift = page.query_selector("//*[@id='ba-student-favoured-items']")
    gift.screenshot(path=path_with_thread_id("./image/tmp/gift.png", thread_id))
    furniture_banner = page.query_selector("//*[@id='ba-student-page-profile']/div[6]/h5")
    furniture_banner.screenshot(path=path_with_thread_id("./image/tmp/furniture_banner.png", thread_id))
    furniture = page.query_selector("//*[@id='ba-student-favoured-furniture']")
    furniture.screenshot(path=path_with_thread_id("./image/tmp/furniture.png", thread_id))

    # 如果有爱用品, 顺带把爱用品的翻译拿到(本地翻译优先) ba-game-db更新有点慢
    # for (let i = 0; i < 11; ++i) { $0.value = i; $0.oninput($0); console.log([...document.querySelectorAll(".ba-col-explosion")][7].innerText) }
    # document.querySelectorAll(".ba-col-explosion")
    gear_info_btn = page.query_selector("#ba-student-tab-gear")
    if gear_info_btn != None and gear_info_btn.is_visible() and "gear_desc" not in dict:
        skill_bounds = re.compile("\d+%")

        gear_info_btn.click()
        time.sleep(2)
        gear_desc_el = page.query_selector("#ba-skill-gearnormal-description")
        gear_desc = gear_desc_el.inner_text()
        gear_desc = skill_bounds.sub("$value", gear_desc).replace("\n", "\\n")
        dict['gear_desc'] = gear_desc

    # 拼到一起
    save_path = path_with_thread_id("./image/tmp/schaledb.png", thread_id)
    path_list = []
    path_list.append(path_with_thread_id("./image/tmp/name_card.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/base_card.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/weapon_name.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/weapon_img.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/live2d_banner.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/live2d.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/gift_banner.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/gift.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/furniture_banner.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/furniture.png", thread_id))

    concat_list(path_list, save_path, margin=0, reshape=True)
    context.close()
    browser.close()
    return dict

def fetch_skill_data_from_schaledb(pl: Playwright, name, thread_id: int):
    """
    从schaledb获取技能截图
    """
    browser = pl.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()
    page.goto("https://schale.gg/?chara=%s" % name)
    page.wait_for_load_state()

    # 关闭change-log窗口
    model = page.query_selector("#modal-changelog")
    if model != None:
        close_btn = model.query_selector(".btn-close")
        if close_btn != None:
            close_btn.click()
    setting_btn = page.query_selector("#ba-navbar-settings")
    setting_btn.click()
    # 换成日服
    region_btn = page.query_selector("#ba-navbar-regionselector")
    region_btn.click()
    region_btn_jp = page.query_selector("#ba-navbar-regionselector-0")
    region_btn_jp.click()
    time.sleep(2)
    # 中文与日语切换确定是否有中文翻译, 如果有就不采用gamekee的机翻
    language_btn = page.query_selector("#ba-navbar-languageselector")
    language_btn.click()
    # 切换成民译
    language_zh_btn = page.query_selector("#ba-navbar-languageselector-zh")
    language_zh_btn.click()
    time.sleep(2)
    setting_btn.click()

    # 删掉背景
    page.evaluate("el => el.remove()", page.query_selector("#ba-background"))

    skill_tab = page.query_selector('//*[@id="ba-student-tab-skills"]')
    if skill_tab != None:
        skill_tab.click()
        time.sleep(1)

    # 限制宽度
    outer = page.query_selector("//*[@id='ba-student']")
    if outer != None:
        outer.evaluate("it => it.style.maxWidth = '500px'")

    # 改成白色
    outer_tab = page.query_selector('//*[@id="ba-student"]/div/div')
    if outer_tab != None:
        outer_tab.evaluate("it => it.style.backgroundColor = 'white'")

    # 来点好看的分割线
    outer_tab.evaluate('it => it.querySelectorAll(".ba-panel-separator").forEach(s => s.style.borderTop = "2px solid")')
    # 小信息背景色
    outer_tab.evaluate('it => it.querySelectorAll(".ba-info-pill-s").forEach(s => s.style.backgroundColor = "var(--col-theme-background)")')

    # 获取攻击类型
    attack_icon = page.query_selector('//*[@id="ba-skill-autoattack-icon"]')
    if attack_icon != None:
        lst: list[str] = attack_icon.evaluate("it => [...it.classList]")
        attack_class = f".ba-col-{lst[-1:][0]}"
    # # 删掉技能图标
    # page.evaluate("document.querySelectorAll('.skill-icon').forEach(it => it.remove())")
    # 删除搜索框
    page.evaluate("document.querySelector('#ba-navbar-placeholder').remove()")
    # 删除hover
    page.evaluate("document.querySelectorAll('[data-bs-original-title]').forEach(it => it.setAttribute('data-bs-original-title',''))")

    # 获取普攻
    autoattack_path = path_with_thread_id("./image/tmp/autoattack.png", thread_id)
    has_autoattack = False
    autoattack = page.query_selector('//*[@id="ba-skill-autoattack"]')
    if autoattack != None and autoattack.is_visible():
        # 分割线
        autoattack.evaluate("it => {const tmp = it.children[0].children[0];tmp.classList.add('pt-2');tmp.style.borderTop = '2px solid'}")
        autoattack.screenshot(path=autoattack_path)
        has_autoattack = True

    def capture_skill_body(slider, body, range: range, name: str):
        res = {}
        for i in range:
            slider.evaluate("it => {it.value = %d; it.oninput();}" % (i))
            res[i] = list(map(lambda x: x.evaluate("it => it.innerText"), body.query_selector_all(attack_class)))
        # 替换并截图
        for idx, it in enumerate(body.query_selector_all(attack_class)):
            it.evaluate("it => it.innerText = '%s'" % "/".join(list(map(lambda x: res[x][idx], range))))
            it.evaluate("it => it.style.wordBreak = 'break-all'")
            it.evaluate("it => it.parentElement.style.whiteSpace = 'normal'")
        # # 删掉技能图标
        # page.evaluate("document.querySelectorAll('.skill-icon').forEach(it => it.remove())")
        body.evaluate('it => it.querySelectorAll(".ba-info-pill-s").forEach(s => s.style.backgroundColor = "var(--col-theme-background)")')
        if name != "":
            body.screenshot(path=name)

    # 获取Ex
    ex_path = path_with_thread_id("./image/tmp/body-ex.png", thread_id)
    ex_body = page.query_selector('//*[@id="ba-student-page-skills"]/div[3]')
    ex_body.evaluate("it => {const tmp = it.children[0];tmp.classList.add('pt-2');tmp.style.borderTop = '2px solid'}")
    ex_body.evaluate('it => it.querySelectorAll(".ba-panel-separator").forEach(s => s.style.borderTop = "2px solid")')
    capture_skill_body(page.query_selector('//*[@id="ba-skillpreview-exrange"]'), ex_body, range(1, 6), ex_path)

    normal_body = page.query_selector('//*[@id="ba-student-page-skills"]/div[5]')
    # 获取其他技能
    # 先拿到完整数据
    capture_skill_body(page.query_selector('//*[@id="ba-skillpreview-range"]'), normal_body, range(1, 11), "")
    # 分技能截图
    # 根据dev拆分
    bs_path = path_with_thread_id("./image/tmp/bs.png", thread_id)
    ns_path = path_with_thread_id("./image/tmp/ns.png", thread_id)
    ss_path = path_with_thread_id("./image/tmp/ss.png", thread_id)
    normal_body.evaluate('it => {const lMap = {0:4,1:3,2:5};for (let index = 0; index < 3; index++) {const el = document.createElement("div");el.classList.add("w-100");el.classList.add("p-2");el.classList.add("tttt");for (let i = 0; i < lMap[index]; i++) {const tmp = it.children[0];tmp.remove();el.appendChild(tmp);}if(index===0){el.children[0].classList.add("pt-2");el.children[0].style.borderTop="2px solid"};it.appendChild(el);}}')
    path_l = [bs_path,ns_path,ss_path]
    for idx, it in enumerate(normal_body.query_selector_all(".tttt")):
        it.screenshot(path=path_l[idx])

    # 爱用品(如果有)
    gear_path = path_with_thread_id("./image/tmp/gear.png", thread_id)
    has_gear = False
    gear_tab = page.query_selector('//*[@id="ba-student-tab-gear"]')
    if gear_tab != None and gear_tab.is_visible():
        gear_tab.click()
        gear_body = page.query_selector('//*[@id="ba-gear-page-t2"]/div[2]')
        # 分割线
        gear_body.evaluate("it => {const tmp = it.children[0];tmp.classList.add('pt-2');tmp.classList.add('w-100');tmp.style.borderTop = '2px solid'}")
        gear_desc = page.query_selector('//*[@id="ba-gear-page-t2"]/div[2]/div/div/div[2]/p/span[2]')
        gear_desc.evaluate("it => it.innerText = it.innerText + '(爱用品T2)'")
        capture_skill_body(page.query_selector('//*[@id="ba-gear-skillpreview-range"]'), gear_body, range(1, 11), gear_path)
        has_gear = True
        # 不知道为什么下边总是有1px的横线
        gear_im = cv2.imdecode(np.fromfile(gear_path, dtype=np.uint8), -1)
        row, col, _ = gear_im.shape
        im = Image.new('RGBA', (col, row-10), color='white')
        im.save(gear_path)
        im = cv2.imdecode(np.fromfile(gear_path, dtype=np.uint8), -1)
        im[0:row,0:col]=gear_im[0:row-10,0:col]
        cv2.imencode(".png", im)[1].tofile(gear_path)

    # 获取专武
    weapon1_path = path_with_thread_id("./image/tmp/weapon1.png", thread_id)
    weapon2_path = path_with_thread_id("./image/tmp/weapon2.png", thread_id)
    weapon_tab = page.query_selector('//*[@id="ba-student-tab-weapon"]')
    if weapon_tab != None:
        weapon_tab.click()
    weapon_body = page.query_selector('//*[@id="ba-weapon-page-2star"]/div[2]')
    # 分割线
    weapon_body.evaluate("it => {const tmp = it.children[0];tmp.classList.add('pt-2');tmp.classList.add('w-100');tmp.style.borderTop = '2px solid'}")

    weapon_desc = page.query_selector('//*[@id="ba-weapon-page-2star"]/div[2]/div/div/div[2]/p/span[2]')
    weapon_desc.evaluate("it => it.innerText = it.innerText + '(固有武器T2)'")
    capture_skill_body(page.query_selector('//*[@id="ba-weapon-skillpreview-range"]'), weapon_body, range(1, 11), weapon1_path)
    # 3星专武效果
    weapon2_tab = page.query_selector('//*[@id="ba-student-page-weapon"]/ul/li[2]/a')
    if weapon2_tab != None:
        weapon2_tab.click()
        time.sleep(1)
    # # 删掉图标
    # page.query_selector('//*[@id="ba-weapon-page-3star"]/div[2]/div').evaluate("it => it.remove()")
    weapon_body2 = page.query_selector('//*[@id="ba-weapon-page-3star"]/div[2]')
    weapon_body2.screenshot(path=weapon2_path)

    # 拼接所有图片
    line1_path = path_with_thread_id("./image/tmp/line-1.png", thread_id)
    line2_path = path_with_thread_id("./image/tmp/line-2.png", thread_id)
    out_path = path_with_thread_id("./image/tmp/out.png", thread_id)
    if has_autoattack:
        concat_two_im(autoattack_path, ex_path, ex_path, type="vertical")
    if has_gear:
        concat_two_im(bs_path, gear_path, bs_path, type="vertical", reshape=True, reshapeType="l")
        concat_two_im(ex_path, bs_path, line1_path)
    else:
        concat_two_im(ex_path, bs_path, line1_path)
    # 专武
    concat_list([ns_path, weapon1_path, weapon2_path], ns_path, reshape=True)
    concat_two_im(ns_path, ss_path, line2_path)
    concat_two_im(line1_path, line2_path, out_path, type="vertical")

    page.close()
    context.close()
    browser.close()
    return out_path

def fetch_data_from_game_db(page: Page, dict, is_no_translate, base_path = "./image/tmp/", thread_id = 0):
        # 下载拉满需要的资源图片之类的
        skill_resource_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[1]/div[2]/div[2]")
        if skill_resource_btn == None:
            skill_resource_btn = page.query_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[2]/div[1]/div/div[1]')
        
        skill_resource_btn.click()

        time.sleep(2)

        # 拿到资源列表的class判断是8行资源还是7行
        target_class = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[4]").get_attribute("class")
        resource_size = len(page.query_selector_all(".%s" % target_class))

        skill_resource_1 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[4]")
        skill_resource_1.screenshot(path=path_with_thread_id("./image/tmp/skill_resource_1.png", thread_id))
        skill_resource_2 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[5]")
        skill_resource_2.screenshot(path=path_with_thread_id("./image/tmp/skill_resource_2.png", thread_id))
        skill_resource_3 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[6]")
        skill_resource_3.screenshot(path=path_with_thread_id("./image/tmp/skill_resource_3.png", thread_id))
        skill_resource_4 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[7]")
        skill_resource_4.screenshot(path=path_with_thread_id("./image/tmp/skill_resource_4.png", thread_id))
        if resource_size == 8:
            skill_resource_5 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[8]")
            skill_resource_5.screenshot(path=path_with_thread_id("./image/tmp/skill_resource_5.png", thread_id))
            resource_1 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[9]")
            resource_1.screenshot(path=path_with_thread_id("./image/tmp/resource_1.png", thread_id))
            resource_2 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[10]")
            resource_2.screenshot(path=path_with_thread_id("./image/tmp/resource_2.png", thread_id))
            resource_3 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[11]")
            resource_3.screenshot(path=path_with_thread_id("./image/tmp/resource_3.png", thread_id))
        else:
            resource_1 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[8]")
            resource_1.screenshot(path=path_with_thread_id("./image/tmp/resource_1.png", thread_id))
            resource_2 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[9]")
            resource_2.screenshot(path=path_with_thread_id("./image/tmp/resource_2.png", thread_id))
            resource_3 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[10]")
            resource_3.screenshot(path=path_with_thread_id("./image/tmp/resource_3.png", thread_id))

        equipment_resource_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[3]/div/div[2]")
        equipment_resource_btn.click()
        time.sleep(6)

        equipment_resource = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[4]")
        equipment_resource.screenshot(path=path_with_thread_id("./image/tmp/equipment_resource.png", thread_id))

        skill_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[1]/div/div[2]")
        skill_btn.click()

        # 替换技能描述文案
        try:
            ex_desc_detail = page.query_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[1]/div/div[3]/span')
            detail_class = ex_desc_detail.get_attribute("class")
            if is_no_translate:
                # ex
                page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[1]/div/div[1]', "node => node.innerText = '%s'" % dict["ex_name"])
                # 基本技能 normal skill
                page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[2]/div/div[1]', "node => node.innerText = '%s'" % dict["bs_name"])
                # 强化技能
                page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[1]/div/div[1]', "node => node.innerText = '%s'" % dict["es_name"])
                # 子技能
                page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[2]/div/div[1]', "node => node.innerText = '%s'" % dict["ss_name"])
                # 拿到具体数据对应的class
                
                detail_list = page.query_selector_all(".%s" % detail_class)

                offset = 0
                def replace(s: str, offset):
                    while s.find("$value") != -1:
                        s = s.replace("$value", '<span class="%s">%s</span>' % (detail_class, detail_list[offset].text_content()), 1)
                        offset = offset + 1
                    return s, offset
                ex_desc, offset = replace(dict["ex_desc"], offset)
                bs_desc, offset = replace(dict["bs_desc"], offset)
                # 如果有爱用品 那就替换
                if "gear_desc" in dict:
                    gear_desc, offset = replace(dict["gear_desc"], offset)
                    # favor usage
                    page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[2]/div/div[4]/div[2]', "node => node.innerHTML = '%s'" % gear_desc)
                es_desc, offset = replace(dict["es_desc"], offset)
                wp_skill, offset = replace(dict["wp_skill"], offset)
                ss_desc, offset = replace(dict["ss_desc"], offset)
                # ex
                page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[1]/div/div[3]', "node => node.innerHTML = '%s'" % ex_desc)
                # bs
                page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[2]/div/div[3]', "node => node.innerHTML = '%s'" % bs_desc)
                # es
                page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[1]/div/div[3]', "node => node.innerHTML = '%s'" % es_desc)
                # ss
                page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[2]/div/div[3]', "node => node.innerHTML = '%s'" % ss_desc)
                # special weapon
                page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[1]/div/div[4]/div[2]', "node => node.innerHTML = '%s'" % wp_skill)
            # ba-game-db 爱用品更新很慢, 直接强制替换
            elif "gear_desc" in dict:

                # 拿到爱用品所在的div
                gear = page.query_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[2]/div/div[4]/div[2]')
                # 有时候会出现有翻译但是没爱用品标签的情况, 跳过
                if gear != None:
                    detail_list = gear.query_selector_all(".%s" % detail_class)
                    s = dict["gear_desc"]
                    for item in detail_list:
                        s = s.replace("$value", '<span class="%s">%s</span>' % (detail_class, item.text_content()), 1)
                    # favor usage
                    # 有时候会出现有翻译但是没爱用品标签的情况, 跳过
                    try:
                        page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[2]/div/div[4]/div[2]', "node => node.innerHTML = '%s'" % s)
                    except Exception as e:
                        pass
            ex_skill = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[1]")
            ex_skill.screenshot(path=path_with_thread_id("./image/tmp/ex_skill.png", thread_id))
            base_skill = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[2]")
            base_skill.screenshot(path=path_with_thread_id("./image/tmp/base_skill.png", thread_id))
            enhance_skill = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[1]")
            enhance_skill.screenshot(path=path_with_thread_id("./image/tmp/enhance_skill.png", thread_id))
            sub_skill = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[2]")
            sub_skill.screenshot(path=path_with_thread_id("./image/tmp/sub_skill.png", thread_id))
        except Exception as _:
            print("error in replace skill desc.")
            pass
        # 拼接技能成长材料
        skill_resource_save_path = path_with_thread_id("./image/tmp/skill_resource.png", thread_id)
        path_list = []
        path_list.append(path_with_thread_id("./image/tmp/skill_resource_1.png", thread_id))
        path_list.append(path_with_thread_id("./image/tmp/skill_resource_2.png", thread_id))
        path_list.append(path_with_thread_id("./image/tmp/skill_resource_3.png", thread_id))
        path_list.append(path_with_thread_id("./image/tmp/skill_resource_4.png", thread_id))
        if resource_size == 8:
            path_list.append(path_with_thread_id("./image/tmp/skill_resource_5.png", thread_id))
        path_list.append(path_with_thread_id("./image/tmp/resource_1.png", thread_id))
        path_list.append(path_with_thread_id("./image/tmp/resource_2.png", thread_id))
        path_list.append(path_with_thread_id("./image/tmp/resource_3.png", thread_id))

        concat_list(path_list, skill_resource_save_path, -3)

        # 将技能材料和装备材料拼在一起
        skill_resource_equipment = path_with_thread_id("./image/tmp/skill_resource_equipment.png", thread_id)
        concat_two_im(skill_resource_save_path, path_with_thread_id("./image/tmp/equipment_resource.png", thread_id), skill_resource_equipment)

        # 将技能描述拼在一起

        concat_two_im(path_with_thread_id("./image/tmp/ex_skill.png", thread_id), path_with_thread_id("./image/tmp/base_skill.png", thread_id), path_with_thread_id("./image/tmp/skill_desc_1.png", thread_id))
        concat_two_im(path_with_thread_id("./image/tmp/enhance_skill.png", thread_id), path_with_thread_id("./image/tmp/sub_skill.png", thread_id), path_with_thread_id("./image/tmp/skill_desc_2.png", thread_id))
        concat_two_im(path_with_thread_id("./image/tmp/skill_desc_1.png", thread_id), path_with_thread_id("./image/tmp/skill_desc_2.png", thread_id), path_with_thread_id("./image/tmp/skill_desc.png", thread_id), type="vertical")

        # 把技能描述和材料拼在一起
        final_path = path_with_thread_id("./image/tmp/game_db.png", thread_id)
        concat_two_im(skill_resource_equipment, path_with_thread_id("./image/tmp/skill_desc.png", thread_id), path=final_path, type="vertical")
        return skill_resource_equipment

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
    resp = requests.get("https://arona.diyigemt.com/api/v2/image?name=%s" % name, headers=header)
    result = json.loads(resp.content.decode())
    return len(result["data"]) == 1

def query_remote_name(name):
    header = {
        "Content-Type": "application/json"
    }
    resp = requests.get("https://arona.diyigemt.com/api/v2/image?name=%s" % name, headers=header)
    result = json.loads(resp.content.decode())
    if len(result["data"]) > 1:
        return query_remote_name(result["data"][0]["name"])
    elif len(result["data"]) == 0:
        return {"name": name}
    return result["data"][0]

def replace_none_char(name: str) -> str:
    return name.replace("（", "(").replace("）", ")")
