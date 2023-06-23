import codecs
import json
import os
import re
import shutil
from config import cache_file_location, cn_translation_location

regex = re.compile(r".*\(.*?([a-zA-Z]+).*?\).*")
copy_suffix_map = {
    "幼女": "Small",
    "私服": "Casual",
    "泳装": "Swimsuit",
    "温泉": "HotSpring",
    "自行车装": "Cycling",
    "正月": "NewYear",
    "兔女郎": "Bunny",
    "应援团": "Cheerleader",
    "女仆": "Maid",
    "运动服": "Track",
    "圣诞节": "Christmas"
}
copy_suffix = copy_suffix_map.keys()
skip_suffix = ["227号温泉乡", "227号温泉浴场"]
loma_cat_map = {
    "Miku": "Hatsune_Miku",
    "Arisu": "Aris",
    "Yuka": "Yuuka"
}
if __name__ == "__main__":
    # 加载dict
    cache_dict = {}
    with codecs.open(cache_file_location, "r", encoding="utf-8") as f:
        cache_dict = json.loads(f.read())
    loma_map = {}
    for jp in cache_dict:
        item = cache_dict[jp]
        loma_map[item["loma"]] = item["cnName"]

    for im in os.listdir("./image/some"):
        file_path = os.path.join("./image/some", im)
        matched_loma = regex.sub(r"\g<1>", file_path).capitalize()
        if matched_loma in loma_cat_map:
            matched_loma = loma_cat_map[matched_loma]
        copy_prefix = ""
        # 判断是不是换皮
        for copy in copy_suffix_map:
            if im.find(copy) != -1:
                if len(list(filter(lambda x: im.find(x) != -1, skip_suffix))) == 0:
                    copy_prefix = copy_suffix_map[copy]
        if copy_prefix != "":
            matched_loma = "%s_%s" % (matched_loma, copy_prefix)
        if matched_loma not in loma_map:
            print("error: %s" % im)
            continue
        name = loma_map[matched_loma]
        new_path = os.path.join("./image/some", name) + ".png"
        shutil.move(file_path, new_path)
        print("success: %s" % name)
    