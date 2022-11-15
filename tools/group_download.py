import yaml
from yaml.loader import SafeLoader
from tools import draw_image
from fetch_student_info_from_ba_game_db import concat_list
import os

if __name__ == "__main__":
    source = {}
    with open(r"./config/group_download.yml", "r", encoding="UTF-8") as f:
        source = yaml.load(f, SafeLoader)
    count = 1
    images = list(
        filter(lambda item: item['name'] != 'test', list(source['image'])))
    total = len(images)
    concat_group = {}
    for item in images:
        print("%d/%d" % (count, total))
        name = item["name"]
        if name.find(".png") == -1:
            name = name + ".png"
        path = item["path"]
        url = item["url"].split("@")[0]
        if "source" in item:
            draw_image(url, name, path, source=item["source"])
        else:
            draw_image(url, name, path)
        if "group" in item and item["group"] != "":
            if item["group"] in concat_group:
                concat_group[item["group"]].append(path + name)
            else:
                concat_group[item["group"]] = [path + name]
        count += 1
    if len(concat_group.keys()) != 0:
        print("start concat")
        for key in concat_group.keys():
            print("concat group: %s" % key)
            group = concat_group[key]
            concat_list(group, group[0], reshape=True)
            group.remove(group[0])
            for f in group:
                os.remove(f)
