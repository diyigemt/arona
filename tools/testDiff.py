import time
from PIL import Image
import cv2
import numpy as np
from playwright.sync_api import Playwright, sync_playwright

from fetch_student_info_from_ba_game_db import concat_list, concat_two_im, fetch_skill_data_from_schaledb

def run(playwright: Playwright):
    fetch_skill_data_from_schaledb(playwright,"Akari_NewYear",1)
    return
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()
    page.goto("https://schale.gg/?chara=Akari_NewYear")
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

    path_list = []

    # 获取普攻
    has_autoattack = False
    autoattack = page.query_selector('//*[@id="ba-skill-autoattack"]')
    if autoattack != None and autoattack.is_visible():
        # 分割线
        autoattack.evaluate("it => {const tmp = it.children[0].children[0];tmp.classList.add('pt-2');tmp.style.borderTop = '2px solid'}")
        autoattack.screenshot(path="./image/tmp/autoattack.png")
        has_autoattack = True
        path_list.append("./image/tmp/autoattack.png")
    
    # 获取Ex
    ex_body = page.query_selector('//*[@id="ba-student-page-skills"]/div[3]')
    ex_body.evaluate("it => {const tmp = it.children[0];tmp.classList.add('pt-2');tmp.style.borderTop = '2px solid'}")
    ex_body.evaluate('it => it.querySelectorAll(".ba-panel-separator").forEach(s => s.style.borderTop = "2px solid")')

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
            path_list.append(name)

    capture_skill_body(page.query_selector('//*[@id="ba-skillpreview-exrange"]'), ex_body, range(1, 6), "./image/tmp/body-ex.png")
    
    normal_body = page.query_selector('//*[@id="ba-student-page-skills"]/div[5]')

    # 获取其他技能
    # 先拿到完整数据
    capture_skill_body(page.query_selector('//*[@id="ba-skillpreview-range"]'), normal_body, range(1, 11), "")
    # 分技能截图
    # 根据dev拆分
    normal_body.evaluate('it => {const lMap = {0:4,1:3,2:5};for (let index = 0; index < 3; index++) {const el = document.createElement("div");el.classList.add("w-100");el.classList.add("p-2");el.classList.add("tttt");for (let i = 0; i < lMap[index]; i++) {const tmp = it.children[0];tmp.remove();el.appendChild(tmp);}if(index===0){el.children[0].classList.add("pt-2");el.children[0].style.borderTop="2px solid"};it.appendChild(el);}}')
    for idx, it in enumerate(normal_body.query_selector_all(".tttt")):
        it.screenshot(path="./image/tmp/skill-%d.png" % idx)
        path_list.append("./image/tmp/skill-%d.png" % idx)

    # 爱用品(如果有)
    has_gear = False
    gear_tab = page.query_selector('//*[@id="ba-student-tab-gear"]')
    if gear_tab != None and gear_tab.is_visible():
        gear_tab.click()
        gear_body = page.query_selector('//*[@id="ba-gear-page-t2"]/div[2]')
        # 分割线
        gear_body.evaluate("it => {const tmp = it.children[0];tmp.classList.add('pt-2');tmp.classList.add('w-100');tmp.style.borderTop = '2px solid'}")
        gear_desc = page.query_selector('//*[@id="ba-gear-page-t2"]/div[2]/div/div/div[2]/p/span[2]')
        gear_desc.evaluate("it => it.innerText = it.innerText + '(爱用品T2)'")
        capture_skill_body(page.query_selector('//*[@id="ba-gear-skillpreview-range"]'), gear_body, range(1, 11), "./image/tmp/gear.png")
        has_gear = True
        # 不知道为什么下边总是有1px的横线
        gear_im = cv2.imdecode(np.fromfile("./image/tmp/gear.png", dtype=np.uint8), -1)
        row, col, _ = gear_im.shape
        im = Image.new('RGBA', (col, row-10), color='white')
        im.save("./image/tmp/gear.png")
        im = cv2.imdecode(np.fromfile("./image/tmp/gear.png", dtype=np.uint8), -1)
        im[0:row,0:col]=gear_im[0:row-10,0:col]
        cv2.imencode(".png", im)[1].tofile("./image/tmp/gear.png")

    # 获取专武
    weapon_tab = page.query_selector('//*[@id="ba-student-tab-weapon"]')
    if weapon_tab != None:
        weapon_tab.click()
    weapon_body = page.query_selector('//*[@id="ba-weapon-page-2star"]/div[2]')
    # 分割线
    weapon_body.evaluate("it => {const tmp = it.children[0];tmp.classList.add('pt-2');tmp.classList.add('w-100');tmp.style.borderTop = '2px solid'}")

    weapon_desc = page.query_selector('//*[@id="ba-weapon-page-2star"]/div[2]/div/div/div[2]/p/span[2]')
    weapon_desc.evaluate("it => it.innerText = it.innerText + '(固有武器T2)'")
    capture_skill_body(page.query_selector('//*[@id="ba-weapon-skillpreview-range"]'), weapon_body, range(1, 11), "./image/tmp/weapon.png")
    # 3星专武效果
    weapon2_tab = page.query_selector('//*[@id="ba-student-page-weapon"]/ul/li[2]/a')
    if weapon2_tab != None:
        weapon2_tab.click()
        time.sleep(1)
    # # 删掉图标
    # page.query_selector('//*[@id="ba-weapon-page-3star"]/div[2]/div').evaluate("it => it.remove()")
    weapon_body2 = page.query_selector('//*[@id="ba-weapon-page-3star"]/div[2]')
    weapon_body2.screenshot(path="./image/tmp/weapon-2.png")
    path_list.append("./image/tmp/weapon-2.png")

    # 拼接所有图片
    if has_autoattack:
        concat_two_im("./image/tmp/autoattack.png", "./image/tmp/body-ex.png", "./image/tmp/body-ex.png", type="vertical")
    if has_gear:
        concat_two_im("./image/tmp/skill-0.png", "./image/tmp/gear.png", "./image/tmp/bs.png", type="vertical", reshape=True, reshapeType="l")
        concat_two_im("./image/tmp/body-ex.png", "./image/tmp/bs.png", "./image/tmp/line-1.png")
    else:
        concat_two_im("./image/tmp/body-ex.png", "./image/tmp/skill-0.png", "./image/tmp/line-1.png")
    # 专武
    concat_list(["./image/tmp/skill-1.png", "./image/tmp/weapon.png", "./image/tmp/weapon-2.png"], "./image/tmp/ns.png", reshape=True)
    concat_two_im("./image/tmp/ns.png", "./image/tmp/skill-2.png", "./image/tmp/line-2.png")
    concat_two_im("./image/tmp/line-1.png", "./image/tmp/line-2.png", "./image/tmp/out.png", type="vertical")

    page.close()


if __name__ == "__main__":
    with sync_playwright() as playwright:
        run(playwright)