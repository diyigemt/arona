from playwright.sync_api import Playwright, sync_playwright, expect


def run(playwright: Playwright) -> None:
    browser = playwright.chromium.launch(headless=False)
    context = browser.new_context()
    page = context.new_page()

    page.goto("https://lonqie.github.io/SchaleDB/?chara=Hoshino")

    page.locator("#ba-weaponpreview-star-3 i").click()

    page.locator("a:has-text(\"装备\")").click()

    page.locator("#ba-statpreview-status-bond-level").click()

    page.locator("#ba-statpreview-status-bond-alt-level").click()

    page.locator("a:has-text(\"Lv.10\")").click()

    page.locator("#ba-statpreview-levelrange").click()

    page.locator("#ba-statpreview-levelrange").click()

    page.locator("#ba-statpreview-levelrange").fill("42")

    page.locator("#ba-statpreview-levelrange").dblclick()

    page.get_by_role("link", name="技能").click()

    page.locator("#ba-skillpreview-exrange").click()

    page.locator("#ba-skillpreview-exrange").fill("3")

    page.locator("#ba-skillpreview-exrange").dblclick()

    page.locator("#ba-skillpreview-range").click()

    page.locator("#ba-skillpreview-range").fill("6")

    page.locator("#ba-skillpreview-range").dblclick()

    page.get_by_role("link", name="固有武器").click()

    page.locator("#ba-weaponpreview-levelrange").click()

    page.locator("#ba-weaponpreview-levelrange").fill("26")

    page.locator("#ba-weaponpreview-levelrange").dblclick()

    page.get_by_role("link", name=" 3").click()

    page.get_by_role("link", name="资料").click()

    # ---------------------
    context.close()
    browser.close()


with sync_playwright() as playwright:
    run(playwright)
