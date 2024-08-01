import time
from playwright.sync_api import Playwright, sync_playwright

def run(playwright: Playwright):
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1950}, device_scale_factor=2.0)
    page = context.new_page()

    # 拿到成长资源截图
    page.goto("https://ba.game-db.tw/")
    page.locator("svg").first.click()
    page.locator("#react-select-2-option-0").click()
    page.locator("text=素材 >> nth=1").click()
    time.sleep(2)
    page.screenshot(path="./image/some/材料一览.png", type="png")
    page.close()
    context.close()
    browser.close()

if __name__ == "__main__":
    with sync_playwright() as playwright:
        run(playwright)