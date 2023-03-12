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
import re
# 要生成的目标 日文名
target = [
    "コユキ",
    # "ジュンコ(正月)",
    # "ハルナ(正月)",
    # "フウカ(正月)"
    # "アカネ(バニーガール)","イズミ(水着)"
    # "アイリ","アカネ","アカネ(バニーガール)","アカリ","アコ","アズサ","アズサ(水着)",
# "アスナ","アスナ(バニーガール)",
# "アツコ","アヤネ",
# "アヤネ(水着)","アリス",
# "アル","アル(正月)","イオリ",
# "イオリ(水着)","イズナ","イズナ(水着)","イズミ","イズミ(水着)","イロハ","ウイ","ウタハ","ウタハ(応援団)","エイミ","カエデ","カズサ","カヨコ","カリン","カリン(バニーガール)","キリノ","ココナ","コタマ","コトリ","コハル","サオリ","サキ","サヤ","サヤ(私服)","シグレ","シズコ","シズコ(水着)","シミコ","ジュリ","シュン","シュン(幼女)","ジュンコ","シロコ","シロコ(ライディング)","スズミ","スミレ","セナ","セリカ","セリカ(正月)","セリナ","セリナ(クリスマス)","チェリノ","チェリノ(温泉)","チセ","チセ(水着)","チナツ","チナツ(温泉)",
# "チヒロ","ツクヨ","ツバキ","ツルギ","ツルギ(水着)","トモエ","ナツ","ネル","ネル(バニーガール)","ノア","ノドカ","ノドカ(温泉)","ノノミ","ノノミ(水着)","ハスミ","ハスミ(体操服)","ハナエ","ハナエ(クリスマス)","ハナコ","ハルカ","ハルナ","ハレ",
# "ヒナ","ヒナ(水着)","ヒナタ","ヒビキ","ヒビキ(応援団)","ヒフミ","ヒフミ(水着)","ヒマリ","ヒヨリ","フィーナ","フウカ","フブキ","ホシノ","ホシノ(水着)","マキ","マシロ","マシロ(水着)","マリー","マリー(体操服)","マリナ","ミサキ","ミチル","ミドリ","ミモリ","ミヤコ","ミユ","ムツキ","ムツキ(正月)","モエ","モモイ","ユウカ","ユウカ(体操服)","ユズ","ヨシミ","ワカモ","ワカモ(水着)"
]
# 如果本地有图片
local_file_path = {"ジュンコ(正月)":"春纯子.png","ハルナ(正月)":"新春狙.png","フウカ(正月)":"春枫香.png","初音ミク":"初音.png","アイリ":"爱莉.png","アカネ":"茜.png","アカネ(バニーガール)":"兔女郎茜.png","アカリ":"亚伽里.png","アコ":"亚子.png","アズサ":"梓.png","アズサ(水着)":"泳装梓.png","アスナ":"明日奈.png","アスナ(バニーガール)":"兔女郎明日奈.png","アツコ":"亚津子.png","アヤネ":"凌音.png","アヤネ(水着)":"泳装绫音.png","アリス":"爱丽丝.png","アル":"阿露.png","アル(正月)":"正月亚瑠.png","イオリ":"伊织.png","イオリ(水着)":"泳装伊织.png","イズナ":"泉奈.png","イズナ(水着)":"泳装泉奈.png","イズミ":"泉.png","イズミ(水着)":"泳装泉.png","イロハ":"伊吕波.png","ウイ":"忧.png","ウタハ":"歌原.png","ウタハ(応援団)":"啦啦队歌原.png","エイミ":"艾米.png","カエデ":"枫.png","カズサ":"和纱.png","カヨコ":"佳代子.png","カリン":"花凛.png","カリン(バニーガール)":"兔女郎花凛.png","キリノ":"桐乃.png","ココナ":"心奈.png","コタマ":"小玉.png","コトリ":"柯托莉.png","コハル":"小春.png","サオリ":"纱织.png","サキ":"咲.png","サヤ":"沙耶.png","サヤ(私服)":"私服沙耶.png","シグレ":"时雨.png","シズコ":"静子.png","シズコ(水着)":"泳装静子.png","シミコ":"志美子.png","ジュリ":"茱莉.png","シュン":"瞬.png","シュン(幼女)":"幼女旬.png","ジュンコ":"淳子.png","シロコ":"白子.png","シロコ(ライディング)":"骑行白子.png","スズミ":"铃美.png","スミレ":"堇.png","セナ":"濑名.png","セリカ":"芹香.png","セリカ(正月)":"正月茜香.png","セリナ":"芹娜.png","セリナ(クリスマス)":"圣诞节芹娜.png","チェリノ":"切里诺.png","チェリノ(温泉)":"温泉切里诺.png","チセ":"千世.png","チセ(水着)":"泳装知世.png","チナツ":"千夏.png","チナツ(温泉)":"温泉千夏.png","チヒロ":"千寻.png","ツクヨ":"月咏.png","ツバキ":"椿.png","ツルギ":"鹤城.png","ツルギ(水着)":"泳装鹤城.png","トモエ":"智惠.png","ナツ":"夏.png","ネル":"尼禄.png","ネル(バニーガール)":"兔女郎尼禄.png","ノア":"诺亚.png","ノドカ":"和香.png","ノドカ(温泉)":"温泉和香.png","ノノミ":"富婆.png","ノノミ(水着)":"泳装野乃美.png","ハスミ":"莲见.png","ハスミ(体操服)":"运动服莲见.png","ハナエ":"花绘.png","ハナエ(クリスマス)":"圣诞节花绘.png","ハナコ":"花子.png","ハルカ":"遥香.png","ハルナ":"晴奈.png","ハレ":"晴.png","ヒナ":"日奈.png","ヒナ(水着)":"泳装阳奈.png","ヒナタ":"日向.png","ヒビキ":"响.png","ヒビキ(応援団)":"拉拉响.png","ヒフミ":"日富美.png","ヒフミ(水着)":"泳装日富美.png","ヒマリ":"日鞠.png","ヒヨリ":"日和.png","フィーナ":"菲娜.png","フウカ":"枫香.png","フブキ":"吹雪.png","ホシノ":"星野.png","ホシノ(水着)":"泳装星野.png","マキ":"真纪.png","マシロ":"真白.png","マシロ(水着)":"泳装真白.png","マリー":"玛莉.png","マリー(体操服)":"运动服玛丽.png","マリナ":"玛丽娜.png","ミサキ":"美咲.png","ミチル":"满.png","ミドリ":"绿.png","ミモリ":"三森.png","ミヤコ":"宫子.png","ミユ":"美游.png","ムツキ":"睦月.png","ムツキ(正月)":"正月睦月.png","モエ":"萌惠.png","モモイ":"桃井.png","ユウカ":"优香.png","ユウカ(体操服)":"运动服佑香.png","ユズ":"柚子.png","ヨシミ":"好美.png","ワカモ":"若藻.png","ワカモ(水着)":"泳装若藻.png","ミネ":"美祢.png","ミカ":"未花.png","メグ":"惠.png","カンナ":"叶渚.png","サクラコ":"樱子.png","トキ":"时.png","ナギサ":"渚.png","コユキ":"小雪.png"}

def run(playwright: Playwright):
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()

    # 拿到成长资源截图
    page.goto("https://ba.game-db.tw/")
    page.locator("svg").first.click()
    page.locator("#react-select-2-option-0").click()
    page.get_by_text("一覧表").click()

    target_class = page.get_by_text("ユウカ（体操服）").get_attribute("class")
    jpNameBtnList = page.query_selector_all(".%s" % target_class)
    print("build btn mapping...")
    for btn in jpNameBtnList:
        page.evaluate("el => el.setAttribute('jpName', '%s')" % replace_none_char(btn.text_content()), btn)
    print("build complete")

    if len(target) == 0:
        exit(0)
    # 加载dict
    cache_dict = {}
    with codecs.open(cache_file_location, "r", encoding="utf-8") as f:
        cache_dict = json.loads(f.read())
    count = 0
    for jpName in target:
        if jpName not in cache_dict:
            count = count + 1
            print("%s not found in dict" % jpName)
            continue
        info = cache_dict[jpName]
        cnName = info["cnName"]
        loma = info["loma"]
        print("start: %s" % cnName)
        # 找到页面上的对应按钮
        btnFilterList = list(filter(lambda btn: btn.get_attribute("jpName") == jpName, jpNameBtnList))
        if len(btnFilterList) != 1:
            count = count + 1
            print("%s not found in btn" % jpName)
            continue
        
        # 切换回中文
        page.locator("svg").first.click()
        page.locator("#react-select-2-option-1").click()
        time.sleep(3)
        count = 0
        start_time = time.time()
        end_time = 0
        # 开始下载远端文件
        remote = query_remote_name(info["cnName"])
        path = str(remote["path"])
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
                print("local file not found, skip: %s, %d/%d" % (cnName, count, 118))
                continue
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
        # 从shaledb下载
        fetch_data_from_schaledb(playwright, loma, cn_info)

        # 从gamedb下载
        btnFilterList[0].click()
        time.sleep(2)
        # 中日切换判断是否有翻译
        cn_skill = page.query_selector('//*[@id="skill1"]/div/div[1]').text_content()
        # 关闭信息窗口
        close_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[1]")
        close_btn.click()
        # 切换回日文
        page.locator("svg").first.click()
        page.locator("#react-select-2-option-0").click()
        btnFilterList[0].click()
        time.sleep(2)
        jp_skill = page.query_selector('//*[@id="skill1"]/div/div[1]').text_content()
        # 关闭信息窗口
        close_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[1]")
        close_btn.click()
        # 切换回中文
        page.locator("svg").first.click()
        page.locator("#react-select-2-option-1").click()
        time.sleep(2)
        btnFilterList[0].click()
        time.sleep(2)
        base_path = "./image/tmp/"
        # 下载拉满需要的资源图片之类的
        fetch_data_from_game_db(page, cn_info, cn_skill == jp_skill and cn_info["ex_name"] != "")

        # 和schaledb的拼在一起

        final_db_im = concat_two_im(base_path + "game_db.png", base_path + "schaledb.png", base_path + "final_db.png")

        # 和夜喵拼在一起

        source_row, source_col, dimension = source_im.shape
        if dimension == 3:
            source_im = cv2.cvtColor(source_im, cv2.COLOR_BGR2BGRA)

        final_db_row, final_db_col, _ = final_db_im.shape
        if final_db_col > source_col:
            pp = base_path + "final_db.png"
            im = Image.open(pp)
            (x, y) = im.size
            rate = source_col / final_db_col
            resize = im.resize((int(x * rate), int(y * rate)), Image.Resampling.LANCZOS)
            resize.save(pp)
            final_db_im = cv2.imdecode(np.fromfile(pp, dtype=np.uint8), -1)
            final_db_row, final_db_col, _ = final_db_im.shape
        col = final_db_col + 10
        row = source_row + final_db_row + 10
        im = Image.new('RGBA', (col, row), color='white')
        im.save(local_path)
        im = cv2.imdecode(np.fromfile(local_path, dtype=np.uint8), -1)
        im[0: source_row, 0: source_col] = source_im
        im[source_row + 10: source_row + 10 + final_db_row, 10: final_db_col + 10] = final_db_im
        # im = cv2.cvtColor(im, cv2.COLOR_BGRA2BGR)
        cv2.imencode(".png", im)[1].tofile(local_path)
        count = count + 1
        end_time = time.time()

        # loacal
        print("success: %s, %d/%d, spend: %ds" % (cnName, count, len(target), (end_time - start_time)))
        
        start_time = end_time
        # 关闭信息窗口
        close_btn = page.query_selector("//*[@id='root']/div/div[2]/div[2]/div[1]")
        close_btn.click()

def get_cn_info_from_gamekee(playwright: Playwright, path: str):
    browser = playwright.chromium.launch(headless=True, slow_mo=100)
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=4.0)
    page = context.new_page()

    page.goto(path)
    time.sleep(2)
    page.eval_on_selector_all(".dailog-data-wrapper", "nodes => nodes.forEach(el => el.remove())")
    time.sleep(2)
    skill_bounds = re.compile("\d+[%％]?[∼～~]\d+[%％]?")


    info = {}
    prefix = [
        '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/span/span/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/span/span/span/span[2]/span/span/span/span/span/div[3]/span/span/span/span[2]/span/span/span/span/div[1]/div[1]/div/div/table/tbody/',
        '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
        '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div/span/span/div/div/span/div/span/div/div/span/div/span/div/div/div/span/div/span/div/div/div[3]/span/div/span/div/div[1]/div[1]/div/div/table/tbody/',
        '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div[2]/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
        '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/div/span/div[1]/span/div/span/div/div/span/div/span/div/div/div/span/div/span/div/div/div[3]/span/div/span/div/div[1]/div[1]/div/div/table/tbody/',
        '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
        '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/span/span/div/div[1]/div[1]/div/div/table/tbody/',
        '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div[3]/span/span/div/span/span[2]/span/span/span/span/div[1]/div[1]/div/div/table/tbody/',
        '//*[@id="app"]/div[1]/div[1]/div[2]/div/div/div[2]/div[1]/div[1]/div[3]/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div/span/span/div/span/span[2]/span/span/span/span/span/div/div/div[3]/span/span/div/span/span[2]/span/div/div[1]/div[1]/div/div/table/tbody/',
    ]
    ex_name = get_content(page, list(map(lambda x: x + 'tr[1]', prefix)), True)
    ex_desc = get_content(page, list(map(lambda x: x + 'tr[2]/td[2]', prefix)))
    
    ex_desc = skill_bounds.sub("$value", ex_desc)
    if ex_desc.find("COST：") != -1:
        ex_desc = ex_desc[0:ex_desc.find("COST：")]

    ns_name = get_content(page, list(map(lambda x: x + 'tr[5]', prefix)), True)
    ns_desc = get_content(page, list(map(lambda x: x + 'tr[6]/td[2]', prefix)))
    ns_desc = skill_bounds.sub("$value", ns_desc)

    bs_name = get_content(page, list(map(lambda x: x + 'tr[9]', prefix)), True)
    bs_desc = get_content(page, list(map(lambda x: x + 'tr[10]/td[2]', prefix)))
    bs_desc = skill_bounds.sub("$value", bs_desc)

    ss_name = get_content(page, list(map(lambda x: x + 'tr[13]', prefix)), True)
    ss_desc = get_content(page, list(map(lambda x: x + 'tr[14]/td[2]', prefix)))
    ss_desc = skill_bounds.sub("$value", ss_desc)

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
    ]

    wp_name = get_content(page, list(map(lambda x: x + 'div[1]', wp_prefix)))
    
    wp_desc_1 = get_content(page, list(map(lambda x: x + 'div[2]', wp_prefix)))
    wp_desc_2 = get_content(page, list(map(lambda x: x + 'div[3]', wp_prefix)))

    wp_skill = get_content(page,
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
    ]
    hobby_extend_list = []
    hobby = get_content(page, list(map(lambda x: x + '/tr[6]/td[2]', desc_prefix)) + hobby_extend_list)
    
    desc_extend_list = []
    desc = get_content(page, list(map(lambda x: x + '/tr[9]/td[2]', desc_prefix)) + desc_extend_list)
    # 处理换行
    desc_list = desc.split("。")
    desc = desc_list[0] + "\n" + "".join(desc_list[1:])

    info["ex_name"] = ex_name
    info["ex_desc"] = ex_desc
    info["ns_name"] = ns_name
    info["ns_desc"] = ns_desc
    info["bs_name"] = bs_name
    info["bs_desc"] = bs_desc
    info["ss_name"] = ss_name
    info["ss_desc"] = ss_desc
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
    for xPath in xPaths:
        el = page.query_selector(xPath)
        if el != None:
            content = el.text_content().replace(".", "").replace(" ", "")
            if isSingleLine:
                content = content.replace("\n", "")
            else:
                content = content.replace("\n", "\\n")
            if content.replace("\\n", "") == '':
                continue
            return content
    # print(xPaths)
    return ""
if __name__ == "__main__":
    with sync_playwright() as playwright:
        run(playwright)