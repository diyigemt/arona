import time
from playwright.sync_api import Playwright, sync_playwright

from fetch_student_info_from_ba_game_db import concat_list

def run(playwright: Playwright):
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()
    page.goto("https://schale.gg/?chara=Hina_Dress")
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
    autoattack = page.query_selector('//*[@id="ba-skill-autoattack"]')
    if autoattack != None and autoattack.is_visible():
        autoattack.screenshot(path="./image/tmp/autoattack.png")
        path_list.append("./image/tmp/autoattack.png")
    
    # 获取Ex
    ex_body = page.query_selector('//*[@id="ba-student-page-skills"]/div[3]')
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
        body.screenshot(path=name)
        path_list.append(name)

    capture_skill_body(page.query_selector('//*[@id="ba-skillpreview-exrange"]'), ex_body, range(1, 6), "./image/tmp/body-ex.png")
    
    normal_body = page.query_selector('//*[@id="ba-student-page-skills"]/div[5]')
    # 获取其他技能
    capture_skill_body(page.query_selector('//*[@id="ba-skillpreview-range"]'), normal_body, range(1, 11), "./image/tmp/body.png")

    # 爱用品(如果有)
    gear_tab = page.query_selector('//*[@id="ba-student-tab-gear"]')
    if gear_tab != None and gear_tab.is_visible():
        gear_tab.click()
        gear_body = page.query_selector('//*[@id="ba-gear-page-t2"]/div[2]')
        gear_desc = page.query_selector('//*[@id="ba-gear-page-t2"]/div[2]/div/div/div[2]/p/span[2]')
        gear_desc.evaluate("it => it.innerText = it.innerText + '(爱用品T2)'")
        capture_skill_body(page.query_selector('//*[@id="ba-gear-skillpreview-range"]'), gear_body, range(1, 11), "./image/tmp/gear.png")

    # 获取专武
    weapon_tab = page.query_selector('//*[@id="ba-student-tab-weapon"]')
    if weapon_tab != None:
        weapon_tab.click()
    weapon_body = page.query_selector('//*[@id="ba-weapon-page-2star"]/div[2]')
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

    concat_list(path_list, "./image/tmp/test.png", margin=0, reshape=True)

    page.close()


if __name__ == "__main__":
    with sync_playwright() as playwright:
        run(playwright)