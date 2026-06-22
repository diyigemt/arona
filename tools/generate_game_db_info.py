import os
import re
import cv2
import math
import time
import json
import codecs
import traceback
import threading
from pathlib import Path
import numpy as np
from PIL import Image
from config import cache_file_location, cn_translation_location
from playwright.sync_api import Playwright, sync_playwright, Route, Request
from fetch_student_info_from_ba_game_db import concat_list, concat_two_im, download_image, fetch_data_from_game_db, fetch_data_from_schaledb, fetch_skill_data_from_schaledb, path_with_thread_id, query_remote_name, replace_none_char, test_name_exist

from tools import confirm_action, draw_image_source
# 要生成的目标 日文名
target = []

# game-db数据更新缓慢, 使用override的数据
use_game_db_override = True
game_db_override_script = "https://ba.game-db.tw/static/main.60fb9fc1a0d8c5d39ee5.js"

ROOT = Path(__file__).resolve().parent
ROUTE_OVERRIDES_JSON = ROOT / "playwright" / "route_overrides.json"

sources_map = {
    "schaledb": "部分学生信息,技能数据来源: https://schaledb.com/",
    "gamedb": "部分样式来源: https://ba.game-db.tw/",
    "gamekee": "部分技能翻译,角色翻译来源: https://ba.gamekee.com/ && https://wiki.biligame.com/ && https://kivo.wiki/",
}

lock = threading.Lock()
max_thread = min(os.cpu_count(), 8)

# 加载dict
cache_dict = {}
with codecs.open(cache_file_location, "r", encoding="utf-8") as f:
    cache_dict = json.loads(f.read())

def game_db_content_override(route: Route, req: Request):
    route.fulfill(path="playwright/fake.js")

def load_route_overrides() -> dict[str, str]:
    if not ROUTE_OVERRIDES_JSON.exists():
        raise FileNotFoundError(
            f"{ROUTE_OVERRIDES_JSON} not found; run build_init_js.py first"
        )
    with codecs.open(str(ROUTE_OVERRIDES_JSON), "r", encoding="utf-8") as f:
        raw = json.load(f)
    resolved: dict[str, str] = {}
    for url, local_path in raw.items():
        path = Path(local_path)
        if not path.is_absolute():
            path = ROOT / path
        resolved[url] = str(path)
    return resolved

def register_route_overrides(page, overrides: dict[str, str]):
    for url, local_path in overrides.items():
        page.route(url, lambda r, _, p=local_path: r.fulfill(path=p))

# game-db 学生详情弹窗的关闭按钮 xpath
GAME_DB_CLOSE_XPATH = "//*[@id='root']/div/div[2]/div[2]/div[1]"

def close_game_db_dialog(page):
    """关闭 game-db 学生详情弹窗(若已打开)。

    只在关闭按钮确实可见时才点击, 弹窗未打开时不做任何操作, 避免误点列表元素。
    整个过程吞掉异常: 它会在 finally / 异常清理路径里被调用, 自身绝不能再抛错。
    """
    try:
        if page.is_closed():
            return
        close_btn = page.query_selector(GAME_DB_CLOSE_XPATH)
        if close_btn is not None and close_btn.is_visible():
            close_btn.click(timeout=5000)
            time.sleep(0.5)
    except Exception:
        pass

def select_game_db_language(page, option_id):
    """切换 game-db 站点语言。option_id: 0=日文, 1=中文。"""
    page.locator("svg").first.click(timeout=10000)
    page.locator("#react-select-2-option-%d" % option_id).click(timeout=10000)
    time.sleep(1)

def build_jp_name_btn_list(page):
    """在日文列表页重新抓取学生按钮句柄, 并把日文名写入每个按钮的 jpName 属性。

    依赖列表里的日文文本定位 class, 因此调用前站点必须处于日文。
    每轮重建可避免复用 run() 开头一次性抓取的旧 ElementHandle:
    站点语言在中/日之间切换会触发 React 重渲染, 旧句柄对应的节点被替换或隐藏,
    再用它点击就会报 "element is not visible"(本次反复出现报错的真正根因)。
    """
    target_class = page.get_by_text("ユウカ(体操服)").get_attribute("class")
    jpNameBtnList = page.query_selector_all(".%s" % target_class)
    for btn in jpNameBtnList:
        page.evaluate("el => el.setAttribute('jpName', '%s')" % replace_none_char(btn.text_content()), btn)
    return jpNameBtnList

def open_student_dialog(page, btn):
    """点击学生按钮打开详情弹窗。

    报错 "element is not visible" 多因上一个学生的弹窗遮罩没关掉, 盖住了列表按钮。
    这里先把按钮滚动到可视区再点击; 首次失败时关闭可能残留的弹窗后重试一次,
    仍失败则抛出, 交由上层 try 跳过该学生。
    """
    # 打开前先清掉可能残留的弹窗, 避免首次点击白等 30 秒超时才去清理
    close_game_db_dialog(page)
    for attempt in range(2):
        try:
            btn.scroll_into_view_if_needed(timeout=10000)
            btn.click(timeout=30000)
            return
        except Exception:
            if attempt == 0:
                close_game_db_dialog(page)
                time.sleep(1)
            else:
                raise

def run(playwright: Playwright, arr: list[str], thread_id: int):
    with codecs.open("./config/local_file_map.json", "r", encoding="utf-8") as f:
        local_file_path = json.load(f)
    browser = playwright.chromium.launch(
        proxy={"server":"http://127.0.0.1:12355"},
        headless=True,
        chromium_sandbox=False,
        args=[r"--disk-cache-dir=D:\tmp\playwright"],
        slow_mo=100
        )
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    context.set_extra_http_headers({"Cache-Control": "max-age=3600"})
    page = context.new_page()
    # 帽子: 1 手套: 2 鞋子: 3 背包: 4 徽章: 5 发卡: 6 护符: 7 手表: 8 项链: 9
    if (use_game_db_override):
        page.add_init_script(path="playwright/init.js")
        page.route(game_db_override_script, game_db_content_override)
        register_route_overrides(page, load_route_overrides())
    # 拿到成长资源截图
    page.goto("https://ba.game-db.tw/")
    page.wait_for_load_state()
    page.locator("svg").first.click()
    page.locator("#react-select-2-option-0").click()
    page.get_by_text("一覧").click()

    print("build btn mapping...")
    jpNameBtnList = build_jp_name_btn_list(page)
    print("build complete")

    # 加载翻译
    cn_translate_dict = {}
    with codecs.open(cn_translation_location, "r", encoding="utf-8") as f:
        cn_translate_dict = json.loads(f.read())

    count = 0
    for jpName in arr:
        try:
            if jpName not in cache_dict:
                count = count + 1
                print("%s not found in dict" % jpName)
                continue
            info = cache_dict[jpName]
            cnName = info["cnName"]
            loma = info["loma"]
            offset = info["offset"] if "offset" in info else 0
            print("start: %s" % cnName)
            # 每轮先回到日文列表并重建按钮句柄:
            # 上一轮结束时语言停在中文, 复用最初一次性抓取的旧 ElementHandle 在重渲染后会变得
            # 不可见, 继续点击会报 "element is not visible"。先关残留弹窗 -> 切回日文 -> 重建句柄。
            close_game_db_dialog(page)
            select_game_db_language(page, 0)
            jpNameBtnList = build_jp_name_btn_list(page)
            # 找到页面上的对应按钮
            btnFilterList = list(filter(lambda btn: btn.get_attribute("jpName") == jpName, jpNameBtnList))
            if len(btnFilterList) == 0:
                count = count + 1
                print("%s not found in btn" % jpName)
                continue
            # 切到日文
            # page.locator("svg").first.click()
            # page.locator("#react-select-2-option-0").click()
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

            # # 获取gamekee的中文翻译
            # if info["gamekee"] != "":
            #     cn_info = get_cn_info_from_gamekee(playwright, info["gamekee"])
            # else:
            #     cn_info = {
            #         "ex_name": ""
            #     }

            # # 有时候gamekee太恶心了拿不到, 手动填上
            # if jpName in cn_translate_dict:
            #     cn_info.update(cn_translate_dict[jpName])
            cn_info = {
                "ex_name": ""
            }
            # 从shaledb下载 如果有爱用品信息 顺便拿到爱用品
            # 专武和学生介绍
            cn_info = fetch_data_from_schaledb(playwright, loma, cn_info, thread_id)
            page.pause()
            # 从gamedb下载
            open_student_dialog(page, btnFilterList[offset])
            time.sleep(2)
            # 中日切换判断是否有翻译
            cn_skill = ""
            # try:
            #     cn_skill = page.query_selector('//*[@id="skill1"]/div/div[1]').text_content()
            # except Exception as e:
            #     cn_skill = ""
            # finally:
            #     # 关闭信息窗口
            #     close_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[1]")
            #     if close_btn != None:
            #         close_btn.click()
            # 切换回日文
            # try:
            #     page.locator("svg").first.click()
            #     page.locator("#react-select-2-option-0").click()
            # except Exception as e:
            #     pass
            # btnFilterList[offset].click()
            time.sleep(2)
            # try:
            #     skill_el = page.query_selector('//*[@id="skill1"]/div/div[1]')
            #     jp_skill = skill_el.text_content() if skill_el is not None else "-1"
            # except Exception as e:
            #     jp_skill = "-1"
            jp_skill = "-1"
            # finally:
            #     # 无论技能读取成功与否, 始终关闭弹窗:
            #     # 1) 避免残留弹窗遮挡下一个学生的列表按钮(本次报错的根因);
            #     # 2) 关闭后下面切换语言的下拉框才不会被弹窗盖住而点击失败。
            #     close_game_db_dialog(page)
            # try:
            #     # 切换回中文
            #     select_game_db_language(page, 1)
            #     time.sleep(2)
            #     open_student_dialog(page, btnFilterList[offset])
            #     time.sleep(2)
            # except Exception as e:
            #     pass
            base_path = "./image/tmp/"
            # 下载拉满需要的资源图片之类的
            try:
                skill_resource_equipment_path = fetch_data_from_game_db(page, cn_info, cn_skill == jp_skill and (("ex_name" in cn_info) and cn_info["ex_name"] != ""), thread_id=thread_id)
            finally:
                # 抓完(或抓取中途异常)都释放弹窗状态, 给下一个学生留一个干净的列表页
                close_game_db_dialog(page)

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
            close_game_db_dialog(page)
        except Exception as e:
            # 单个文件处理失败时跳过, 继续处理下一个文件
            count = count + 1
            print("error: %s, %d/%d, skip" % (jpName, count, len(arr)))
            traceback.print_exc()
            # 尝试关闭可能残留的信息窗口, 避免影响下一个文件的处理
            try:
                close_game_db_dialog(page)
            except Exception:
                pass
            # 页面或浏览器已崩溃时, 继续循环只会不断失败, 直接结束该线程。
            # 探测状态本身也可能抛错(连接已断), 一并视为致命。
            try:
                fatal = page.is_closed() or not browser.is_connected() or "crash" in str(e).lower()
            except Exception:
                fatal = True
            if fatal:
                print("fatal: page/browser unusable, stop thread")
                break
            continue

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
            b = len(target)
            for key in cache_dict:
                raw = cache_dict[key]
                if raw["cnName"] == file_name:
                    target.append(key)
                    break
            if len(target) - b == 0:
                print(f"{file_name}不在student_cache中")
    
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