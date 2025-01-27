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
from functools import reduce
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
    row_a, col_a, sa = im_a.shape
    row_b, col_b, sb = im_b.shape
    if sa == 3:
        tmp_im = Image.open(path_a)
        tmp_im = tmp_im.convert("RGBA")
        tmp_im.save(path_a)
        im_a = cv2.imdecode(np.fromfile(path_a, dtype=np.uint8), -1)
    if sb == 3:
        tmp_im = Image.open(path_b)
        tmp_im = tmp_im.convert("RGBA")
        tmp_im.save(path_b)
        im_b = cv2.imdecode(np.fromfile(path_b, dtype=np.uint8), -1)
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
    browser = pl.chromium.launch(
        proxy={"server":"http://127.0.0.1:12350"},
        headless=True,
        chromium_sandbox=False,
        args=[r"--disk-cache-dir=D:\tmp\playwright"],
        slow_mo=100
        )
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    context.set_extra_http_headers({"Cache-Control": "max-age=3600"})
    page = context.new_page()

    # 设置简中 民译 武器50级 好感20级
    page.add_init_script(path="playwright/fxxkPlaywwright.js")

    page.goto("https://schaledb.com/student/%s" % name)
    page.wait_for_load_state()
    
    # # 关闭change-log窗口
    # model = page.query_selector_all(".show")
    # if len(model) != 0:
    #     close_btn = page.query_selector(".btn-close-white")
    #     if close_btn != None:
    #         close_btn.click()

    # # 关闭change-log窗口
    # model = page.query_selector("#modal-changelog")
    # if model != None:
    #     close_btn = model.query_selector(".btn-close")
    #     if close_btn != None:
    #         try:
    #             close_btn.click()
    #         except Exception as _:
    #             pass

    # 删掉背景
    page.evaluate('() => { document.querySelector("#ba-background-back")?.remove(); document.querySelector("#ba-background-front")?.remove() }')
    page.evaluate('() => { const tmp = document.querySelector("#bg-overlay"); if (tmp) { tmp.style.backgroundColor = "white" } }')
    time.sleep(2)
    page.evaluate('() => { document.querySelector("#ba-background-back")?.remove(); document.querySelector("#ba-background-front")?.remove() }')
    page.evaluate('() => { const tmp = document.querySelector("#bg-overlay"); if (tmp) { tmp.style.backgroundColor = "white" } }')
    time.sleep(2)

    # 立绘
    # page.query_selector("#ba-student-img").screenshot(path=path_with_thread_id("./image/tmp/stu.png", thread_id), type="png")
    weapon_btn = page.get_by_role("button", name="固有武器")
    if weapon_btn != None:
        try:
            weapon_btn.click(500)
        except Exception as _:
            page.evaluate("() => { document.querySelectorAll('button.nav-link')[1].click() }")
    time.sleep(2)
    weapon_name = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div[1]')
    weapon_name.evaluate('el => el.style.backgroundColor = "rgb(222, 226, 230, 0.6)"')
    weapon_name.screenshot(path=path_with_thread_id("./image/tmp/weapon_name.png", thread_id), type="png")
    
    weapon_img = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div[2]')
    weapon_img.evaluate('el => el.style.backgroundColor = "rgb(222, 226, 230, 0.4)"')
    weapon_img.screenshot(path=path_with_thread_id("./image/tmp/weapon_img.png", thread_id), type="png")
    base_info_btn = page.get_by_role("button", name="简介")
    base_info_btn.click()
    time.sleep(2)
    # 删除礼物的显示所有和羁绊等级进度条
    show_all_gift = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div[6]/button')
    show_all_gift.evaluate("el => el.remove()")
    favor_progress = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div[3]/div[2]')
    favor_progress.evaluate("el => el.remove()")

    name_card = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div[1]')
    name_card.evaluate('el => el.style.backgroundColor = "rgb(222, 226, 230, 0.6)"')
    name_card.screenshot(path=path_with_thread_id("./image/tmp/name_card.png", thread_id), type="png")

    base_card = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/table')
    base_card.evaluate('el => el.style.backgroundColor = "rgb(222, 226, 230, 0.4)"')
    base_card.screenshot(path=path_with_thread_id("./image/tmp/base_card.png", thread_id), type="png")

    live2d_bannder = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div[2]')
    live2d_bannder.evaluate('el => el.style.backgroundColor = "rgb(222, 226, 230, 0.6)"')
    live2d_bannder.screenshot(path=path_with_thread_id("./image/tmp/live2d_banner.png", thread_id), type="png")

    live2d = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div[3]')
    live2d.evaluate('el => el.style.backgroundColor = "rgb(222, 226, 230, 0.4)"')
    live2d.screenshot(path=path_with_thread_id("./image/tmp/live2d.png", thread_id), type="png")

    live2d_2 = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div[4]')
    live2d_2.evaluate('el => el.style.backgroundColor = "rgb(222, 226, 230, 0.4)"')
    live2d_2.screenshot(path=path_with_thread_id("./image/tmp/live2d_2.png", thread_id), type="png")

    gift_banner = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div[6]')
    gift_banner.evaluate('el => el.style.backgroundColor = "rgb(222, 226, 230, 0.6)"')
    gift_banner.screenshot(path=path_with_thread_id("./image/tmp/gift_banner.png", thread_id), type="png")

    gift = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div[7]')
    gift.evaluate('el => el.style.backgroundColor = "rgb(222, 226, 230, 0.4)"')
    gift.screenshot(path=path_with_thread_id("./image/tmp/gift.png", thread_id), type="png")

    furniture_banner = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div[8]')
    furniture_banner.evaluate('el => el.style.backgroundColor = "rgb(222, 226, 230, 0.6)"')
    furniture_banner.screenshot(path=path_with_thread_id("./image/tmp/furniture_banner.png", thread_id), type="png")

    furniture = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div[9]')
    furniture.evaluate('el => el.style.backgroundColor = "rgb(222, 226, 230, 0.4)"')
    furniture.screenshot(path=path_with_thread_id("./image/tmp/furniture.png", thread_id), type="png")

    # 如果有爱用品, 顺带把爱用品的翻译拿到(本地翻译优先) ba-game-db更新有点慢
    # for (let i = 0; i < 11; ++i) { $0.value = i; $0.oninput($0); console.log([...document.querySelectorAll(".ba-col-explosion")][7].innerText) }
    # document.querySelectorAll(".ba-col-explosion")
    # gear_info_btn = page.query_selector('//*[@id="ba-item-list-tabs"]/button[4]') if page.query_selector('//*[@id="ba-item-list-tabs"]/button[6]') != None else None
    # if gear_info_btn != None and gear_info_btn.is_visible() and "gear_desc" not in dict:
    #     skill_bounds = re.compile("\d+%")

    #     gear_info_btn.click()
    #     time.sleep(2)
    #     gear_desc_el = page.query_selector("#ba-skill-gearnormal-description")
    #     gear_desc = gear_desc_el.inner_text()
    #     gear_desc = skill_bounds.sub("$value", gear_desc).replace("\n", "\\n")
    #     dict['gear_desc'] = gear_desc

    # 拼到一起
    save_path = path_with_thread_id("./image/tmp/schaledb.png", thread_id)
    path_list = []
    path_list.append(path_with_thread_id("./image/tmp/name_card.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/base_card.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/weapon_name.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/weapon_img.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/live2d_banner.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/live2d.png", thread_id))
    path_list.append(path_with_thread_id("./image/tmp/live2d_2.png", thread_id))
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
    browser = pl.chromium.launch(
        proxy={"server":"http://127.0.0.1:12350"},
        headless=True,
        chromium_sandbox=False,
        args=[r"--disk-cache-dir=D:\tmp\playwright"],
        slow_mo=100
        )
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    context.set_extra_http_headers({"Cache-Control": "max-age=3600"})
    page = context.new_page()

    # 设置简中 民译 武器50级 好感20级
    page.add_init_script(path="playwright/fxxkPlaywwright.js")

    page.goto("https://schaledb.com/student/%s" % name)
    page.wait_for_load_state()

    # # 关闭change-log窗口
    # model = page.query_selector_all(".show")
    # if len(model) != 0:
    #     close_btn = page.query_selector(".btn-close-white")
    #     if close_btn != None:
    #         close_btn.click()

    # # 关闭change-log窗口
    # model = page.query_selector("#modal-changelog")
    # if model != None:
    #     close_btn = model.query_selector(".btn-close")
    #     if close_btn != None:
    #         close_btn.click()
    # 删掉背景
    page.evaluate('() => { document.querySelector("#ba-background-back")?.remove(); document.querySelector("#ba-background-front")?.remove() }')
    page.evaluate('() => { const tmp = document.querySelector("#bg-overlay"); if (tmp) { tmp.style.backgroundColor = "white" } }')

    skill_btn = page.get_by_role("button", name="技能", exact=True)
    skill_btn.click()
    time.sleep(2)

    # 限制宽度
    outer = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div')
    if outer != None:
        outer.evaluate("it => it.style.maxWidth = '500px'")

    # 改成白色
    outer_tab = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div')
    if outer_tab != None:
        outer_tab.evaluate("it => it.style.backgroundColor = 'white'")

    # 来点好看的分割线
    outer_tab.evaluate('it => it.querySelectorAll(".ba-panel-separator").forEach(s => s.style.borderTop = "2px solid")')
    # 小信息背景色
    outer_tab.evaluate('it => it.querySelectorAll(".ba-info-pill-s").forEach(s => s.style.backgroundColor = "var(--col-theme-background)")')

    # 获取前后排
    special_text = page.query_selector('.font-nexon')
    if special_text != None:
        is_special = special_text.text_content() == "后援"

    # 获取攻击类型
    attack_icon = page.query_selector('.text-hits')
    if attack_icon != None:
        lst: list[str] = attack_icon.evaluate("it => [...it.classList]")
        attack_class = None
        for clazz in lst:
            if clazz.startswith("ba-col"):
                attack_class = f".{clazz}"
                break
    else:
        print(f"error in forEach parse attack_class, using another function..., loma={name}")
        print(f"warming, is a SPECIAL student? loma={name}")
        is_special = True
        attack_icon = page.query_selector('.skill-icon')
        if attack_icon != None:
            attack_icon_style = attack_icon.evaluate("it => it.style.cssText")
            reg = re.compile(r"--bg-color: var\(--col-bg-(\w+)\);")
            reg_res = reg.search(attack_icon_style)
            if reg_res != None:
                attack_class = f".ba-col-{reg_res.group(1).lower()}"
            else:
                print(f"error in parse attack_class -2, loma={name}")
                attack_class = ".ba-col-explosion" 
        else:
            print(f"error in forEach parse attack_class, loma={name}")
            attack_class = ".ba-col-explosion"
    # 删掉下划线
    page.evaluate("""
        () => {
            document.querySelectorAll(".text-hits").forEach(el => el.style.textDecoration = "none");
        }
""")
    # # 删掉技能图标
    # page.evaluate("document.querySelectorAll('.skill-icon').forEach(it => it.remove())")
    # 删除搜索框
    page.evaluate("document.querySelector('.navbar').remove()")
    # 删除写死的top
    page.evaluate('document.querySelector("#ba-content").setAttribute("style", "top: 0px !important;")')
    # 删除hover
    # page.evaluate("document.querySelectorAll('[data-bs-original-title]').forEach(it => it.setAttribute('data-bs-original-title',''))")

    # 获取普攻
    autoattack_path = path_with_thread_id("./image/tmp/autoattack.png", thread_id)
    has_autoattack = False
    autoattack = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div/div[2]')
    if autoattack != None and autoattack.is_visible() and (not is_special):
        # 分割线
        autoattack.evaluate("it => {const tmp = it.children[0].children[0].children[0];tmp.classList.add('pt-2');tmp.classList.add('w-100');tmp.style.borderTop = '2px solid'}")
        autoattack.screenshot(path=autoattack_path, type="png")
        has_autoattack = True

    def capture_skill_body(slider, body, range: range, name: str):
        res = {}
        # ._vei.onInput({target:{valueAsNumber:5}})
        for i in range:
            slider.evaluate("it => {it._vei.onInput({target:{valueAsNumber:%d}});}" % (i))
            res[i] = list(map(lambda x: str(x.evaluate("it => it.innerText")).replace("\n",""), body.query_selector_all(attack_class)))
        # 替换并截图
        for idx, it in enumerate(body.query_selector_all(attack_class)):
            it.evaluate("it => it.innerText = '%s'" % "/".join(list(map(lambda x: res[x][idx] if res[x][idx] != "" else "-", range))))
            it.evaluate("it => it.style.wordBreak = 'break-all'")
            it.evaluate("it => it.parentElement.style.whiteSpace = 'normal'")
        # # 删掉技能图标
        # page.evaluate("document.querySelectorAll('.skill-icon').forEach(it => it.remove())")
        body.evaluate('it => it.querySelectorAll(".ba-info-pill-s").forEach(s => s.style.backgroundColor = "var(--col-theme-background)")')
        if name != "":
            body.screenshot(path=name, type="png")

    if is_special:
        ex_body = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div/div[2]/div[1]/div[1]')
        normal_body = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div/div[3]/div[1]/div[1]')
    else:
        ex_body = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div/div[3]/div[1]/div[1]')
        normal_body = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div/div[4]/div[1]/div[1]')
    ex_skill_slider = page.query_selector_all('input.flex-fill')[1]
    skill_slider = page.query_selector_all('input.flex-fill')[2]
    
    # 获取Ex
    ex_path = path_with_thread_id("./image/tmp/body-ex.png", thread_id)
    ex_body.evaluate("it => {const tmp = it.children[0];tmp.classList.add('pt-2');tmp.classList.add('w-100');tmp.style.borderTop = '2px solid'}")
    ex_body.evaluate('it => it.querySelectorAll(".ba-panel-separator").forEach(s => s.style.borderTop = "2px solid")')
    capture_skill_body(ex_skill_slider, ex_body, range(1, 6), ex_path)
    
    # 获取其他技能
    # 先拿到完整数据
    capture_skill_body(skill_slider, normal_body, range(1, 11), "")
    # 分技能截图
    # 根据dev拆分
    bs_path = path_with_thread_id("./image/tmp/bs.png", thread_id)
    ns_path = path_with_thread_id("./image/tmp/ns.png", thread_id)
    ss_path = path_with_thread_id("./image/tmp/ss.png", thread_id)
    normal_body.evaluate("""
        it => { 
            let size = it.children.length;
            for (let index = 0; index < 3; index++) {
                const el = document.createElement("div");
                el.classList.add("w-100");
                el.classList.add("p-2");
                el.classList.add("tttt");
                let tmp = it.children[0];
                while(size > 0 && tmp && !tmp.className.startsWith("ba-panel-separator")) {
                    tmp.remove();
                    el.appendChild(tmp);
                    tmp = it.children[0];
                    size--;
                }
                if (tmp.className.startsWith("ba-panel-separator")) {
                    tmp.remove();
                    size--;
                }
                if (index === 0) {
                    el.children[0].classList.add("pt-2");
                    el.children[0].style.borderTop = "2px solid"
                };
                it.appendChild(el);
            }
            it.querySelectorAll(".tttt").forEach(el => {
                el.children[0].classList.add("pt-2");
                el.children[0].style.borderTop = "2px solid";
            })
        }
""")
    path_l = [bs_path,ns_path,ss_path]
    for idx, it in enumerate(normal_body.query_selector_all(".tttt")):
        it.screenshot(path=path_l[idx], type="png")

    # 爱用品(如果有)
    gear_path = path_with_thread_id("./image/tmp/gear.png", thread_id)
    gear_image_path = path_with_thread_id("./image/tmp/gear-1.png", thread_id)
    has_gear = False
    gear_tab = page.get_by_role("button", name="爱用品")
    if gear_tab != None and gear_tab.is_visible():
        gear_tab.click()
        gear_body = page.query_selector('//*[@id="gear-t2"]/div[2]/div/div[1]')
        gear_image = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/div[2]')
        # 分割线
        gear_image.evaluate("it => {const tmp = it.children[0];tmp.classList.add('pt-2');tmp.classList.add('w-100');tmp.style.borderTop = '2px solid'}")
        gear_desc = page.query_selector('//*[@id="gear-t2"]/div[2]/div/div[1]/div[1]/div[2]/div[2]/span')
        gear_desc.evaluate("it => it.innerText = it.innerText + '(爱用品T2)'")
        capture_skill_body(page.query_selector('//*[@id="gear-t2"]/div[2]/div/div[2]/input'), gear_body, range(1, 11), gear_path)
        gear_image.screenshot(path=gear_image_path, type="png")
        has_gear = True
        # 不知道为什么下边总是有1px的横线
        # tmp_im = Image.open(gear_path)
        # tmp_im = tmp_im.convert("RGBA")
        # tmp_im.save(gear_path)
        # gear_im = cv2.imdecode(np.fromfile(gear_path, dtype=np.uint8), -1)
        # row, col, _ = gear_im.shape
        # im = Image.new('RGBA', (col, row-10), color='white')
        # im.save(gear_path)
        # im = cv2.imdecode(np.fromfile(gear_path, dtype=np.uint8), -1)
        # im[0:row,0:col]=gear_im[0:row-10,0:col]
        # cv2.imencode(".png", im)[1].tofile(gear_path)

    # 获取专武
    weapon1_path = path_with_thread_id("./image/tmp/weapon1.png", thread_id)
    weapon2_path = path_with_thread_id("./image/tmp/weapon2.png", thread_id)
    weapon_tab = page.get_by_role("button", name="固有武器")
    if weapon_tab != None:
        try:
            weapon_tab.click(500)
        except Exception as _:
            page.evaluate("() => { document.querySelectorAll('button.nav-link')[1].click() }")
    weapon_body = page.query_selector('//*[@id="weapon-2star"]/div[2]/div/div[1]')
    # 分割线
    weapon_body.evaluate("it => {const tmp = it.children[0];tmp.classList.add('pt-2');tmp.classList.add('w-100');tmp.style.borderTop = '2px solid'}")

    weapon_desc = page.query_selector('//*[@id="weapon-2star"]/div[2]/div/div[1]/div[1]/div[2]/div[2]/span')
    weapon_desc.evaluate("it => it.innerText = it.innerText + '(固有武器T2)'")
    capture_skill_body(page.query_selector('//*[@id="weapon-2star"]/div[2]/div/div[2]/input'), weapon_body, range(1, 11), weapon1_path)
    # 3星专武效果
    weapon2_tab = page.query_selector('//*[@id="ba-content"]/main/div/div/div[2]/div/div[2]/div/ul/li[2]/a')
    if weapon2_tab != None:
        weapon2_tab.click()
        time.sleep(1)
    # # 删掉图标
    # page.query_selector('//*[@id="ba-weapon-page-3star"]/div[2]/div').evaluate("it => it.remove()")
    weapon_body2 = page.query_selector('//*[@id="weapon-3star"]/div[2]')
    weapon_body2.screenshot(path=weapon2_path, type="png")

    # 拼接所有图片
    line1_path = path_with_thread_id("./image/tmp/line-1.png", thread_id)
    line2_path = path_with_thread_id("./image/tmp/line-2.png", thread_id)
    out_path = path_with_thread_id("./image/tmp/out.png", thread_id)
    if has_autoattack:
        concat_two_im(autoattack_path, ex_path, ex_path, type="vertical")
    if has_gear:
        concat_two_im(gear_image_path, gear_path, gear_path, type="vertical", reshape=True, reshapeType="l")
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
        skill_resource_1.screenshot(path=path_with_thread_id("./image/tmp/skill_resource_1.png", thread_id), type="png")
        skill_resource_2 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[5]")
        skill_resource_2.screenshot(path=path_with_thread_id("./image/tmp/skill_resource_2.png", thread_id), type="png")
        skill_resource_3 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[6]")
        skill_resource_3.screenshot(path=path_with_thread_id("./image/tmp/skill_resource_3.png", thread_id), type="png")
        skill_resource_4 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[7]")
        skill_resource_4.screenshot(path=path_with_thread_id("./image/tmp/skill_resource_4.png", thread_id), type="png")
        if resource_size == 8:
            skill_resource_5 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[8]")
            skill_resource_5.screenshot(path=path_with_thread_id("./image/tmp/skill_resource_5.png", thread_id), type="png")
            resource_1 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[9]")
            resource_1.screenshot(path=path_with_thread_id("./image/tmp/resource_1.png", thread_id), type="png")
            resource_2 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[10]")
            resource_2.screenshot(path=path_with_thread_id("./image/tmp/resource_2.png", thread_id), type="png")
            resource_3 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[11]")
            resource_3.screenshot(path=path_with_thread_id("./image/tmp/resource_3.png", thread_id), type="png")
        else:
            resource_1 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[8]")
            resource_1.screenshot(path=path_with_thread_id("./image/tmp/resource_1.png", thread_id), type="png")
            resource_2 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[9]")
            resource_2.screenshot(path=path_with_thread_id("./image/tmp/resource_2.png", thread_id), type="png")
            resource_3 = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[10]")
            resource_3.screenshot(path=path_with_thread_id("./image/tmp/resource_3.png", thread_id), type="png")

        equipment_resource_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[3]/div/div[2]")
        equipment_resource_btn.click()
        time.sleep(6)

        equipment_resource = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[4]")
        equipment_resource.screenshot(path=path_with_thread_id("./image/tmp/equipment_resource.png", thread_id), type="png")

        # skill_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[1]/div/div[2]")
        # skill_btn.click()

        # # 替换技能描述文案
        # try:
        #     ex_desc_detail = page.query_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[1]/div/div[3]/span')
        #     detail_class = ex_desc_detail.get_attribute("class")
        #     if is_no_translate:
        #         # ex
        #         page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[1]/div/div[1]', "node => node.innerText = '%s'" % dict["ex_name"])
        #         # 基本技能 normal skill
        #         page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[2]/div/div[1]', "node => node.innerText = '%s'" % dict["bs_name"])
        #         # 强化技能
        #         page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[1]/div/div[1]', "node => node.innerText = '%s'" % dict["es_name"])
        #         # 子技能
        #         page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[2]/div/div[1]', "node => node.innerText = '%s'" % dict["ss_name"])
        #         # 拿到具体数据对应的class
                
        #         detail_list = page.query_selector_all(".%s" % detail_class)

        #         offset = 0
        #         def replace(s: str, offset):
        #             while s.find("$value") != -1:
        #                 s = s.replace("$value", '<span class="%s">%s</span>' % (detail_class, detail_list[offset].text_content()), 1)
        #                 offset = offset + 1
        #             return s, offset
        #         ex_desc, offset = replace(dict["ex_desc"], offset)
        #         bs_desc, offset = replace(dict["bs_desc"], offset)
        #         # 如果有爱用品 那就替换
        #         if "gear_desc" in dict:
        #             gear_desc, offset = replace(dict["gear_desc"], offset)
        #             # favor usage
        #             page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[2]/div/div[4]/div[2]', "node => node.innerHTML = '%s'" % gear_desc)
        #         es_desc, offset = replace(dict["es_desc"], offset)
        #         wp_skill, offset = replace(dict["wp_skill"], offset)
        #         ss_desc, offset = replace(dict["ss_desc"], offset)
        #         # ex
        #         page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[1]/div/div[3]', "node => node.innerHTML = '%s'" % ex_desc)
        #         # bs
        #         page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[2]/div/div[3]', "node => node.innerHTML = '%s'" % bs_desc)
        #         # es
        #         page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[1]/div/div[3]', "node => node.innerHTML = '%s'" % es_desc)
        #         # ss
        #         page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[2]/div/div[3]', "node => node.innerHTML = '%s'" % ss_desc)
        #         # special weapon
        #         page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[1]/div/div[4]/div[2]', "node => node.innerHTML = '%s'" % wp_skill)
        #     # ba-game-db 爱用品更新很慢, 直接强制替换
        #     elif "gear_desc" in dict:

        #         # 拿到爱用品所在的div
        #         gear = page.query_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[2]/div/div[4]/div[2]')
        #         # 有时候会出现有翻译但是没爱用品标签的情况, 跳过
        #         if gear != None:
        #             detail_list = gear.query_selector_all(".%s" % detail_class)
        #             s = dict["gear_desc"]
        #             for item in detail_list:
        #                 s = s.replace("$value", '<span class="%s">%s</span>' % (detail_class, item.text_content()), 1)
        #             # favor usage
        #             # 有时候会出现有翻译但是没爱用品标签的情况, 跳过
        #             try:
        #                 page.eval_on_selector('//*[@id="root"]/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[2]/div/div[4]/div[2]', "node => node.innerHTML = '%s'" % s)
        #             except Exception as e:
        #                 pass
        #     ex_skill = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[1]")
        #     ex_skill.screenshot(path=path_with_thread_id("./image/tmp/ex_skill.png", thread_id), type="png")
        #     base_skill = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[1]/div[5]/div[2]")
        #     base_skill.screenshot(path=path_with_thread_id("./image/tmp/base_skill.png", thread_id), type="png")
        #     enhance_skill = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[1]")
        #     enhance_skill.screenshot(path=path_with_thread_id("./image/tmp/enhance_skill.png", thread_id), type="png")
        #     sub_skill = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[2]/div[2]/div[2]/div[2]")
        #     sub_skill.screenshot(path=path_with_thread_id("./image/tmp/sub_skill.png", thread_id), type="png")
        # except Exception as _:
        #     print("error in replace skill desc.")
        #     pass
        # # 拼接技能成长材料
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

        # concat_two_im(path_with_thread_id("./image/tmp/ex_skill.png", thread_id), path_with_thread_id("./image/tmp/base_skill.png", thread_id), path_with_thread_id("./image/tmp/skill_desc_1.png", thread_id))
        # concat_two_im(path_with_thread_id("./image/tmp/enhance_skill.png", thread_id), path_with_thread_id("./image/tmp/sub_skill.png", thread_id), path_with_thread_id("./image/tmp/skill_desc_2.png", thread_id))
        # concat_two_im(path_with_thread_id("./image/tmp/skill_desc_1.png", thread_id), path_with_thread_id("./image/tmp/skill_desc_2.png", thread_id), path_with_thread_id("./image/tmp/skill_desc.png", thread_id), type="vertical")

        # # 把技能描述和材料拼在一起
        # final_path = path_with_thread_id("./image/tmp/game_db.png", thread_id)
        # concat_two_im(skill_resource_equipment, path_with_thread_id("./image/tmp/skill_desc.png", thread_id), path=final_path, type="vertical")
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
    resp = requests.get(proxies={},url="https://arona.diyigemt.com/api/v2/image?name=%s" % name, headers=header)
    result = json.loads(resp.content.decode())
    if len(result["data"]) > 1:
        return query_remote_name(result["data"][0]["name"])
    elif len(result["data"]) == 0:
        return {"name": name}
    return result["data"][0]

def replace_none_char(name: str) -> str:
    return name.replace("（", "(").replace("）", ")")
