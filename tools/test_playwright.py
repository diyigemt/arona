from playwright.sync_api import Playwright, sync_playwright, Route, Request

def content_override(route: Route, req: Request):
    route.fulfill(path="playwright/fake.js")
    pass

def run(playwright: Playwright):
    browser = playwright.chromium.launch(
        proxy={"server":"http://127.0.0.1:7890"},
        headless=False,
        chromium_sandbox=False,
        args=[r"--disk-cache-dir=D:\tmp\playwright"],
        slow_mo=100
    )
    context = browser.new_context(viewport={'width': 1920, 'height': 1080})
    context.set_extra_http_headers({"Cache-Control": "max-age=3600"})
    page = context.new_page()
    page.add_init_script(path="playwright/init.js")
    page.route("https://ba.game-db.tw/static/main.276d869eba0ea14c09b6.js", content_override)
    page.goto("https://ba.game-db.tw/")
    page.wait_for_load_state()
    page.locator("svg").first.click()
    page.locator("#react-select-2-option-0").click()
    page.get_by_text("一覧").click()
    page.pause()
    print(1)
    print(2)


if __name__ == "__main__":
    with sync_playwright() as playwright:
        run(playwright)