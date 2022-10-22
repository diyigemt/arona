import yaml
from yaml.loader import SafeLoader
from tools import draw_image

if __name__ == "__main__":
    source = {}
    with open(r"./group_download.yml", "r", encoding="UTF-8") as f:
        source = yaml.load(f, SafeLoader)
    count = 1
    images = list(filter(lambda item: item['name'] != 'test', list(source['image'])))
    total = len(images)
    for item in images:
        print("%d/%d" % (count, total))
        name = item["name"]
        if name.find(".png") == -1:
            name = name + ".png"
        path = item["path"]
        url = item["url"].split("@")[0]
        draw_image(url, name, path)
        count += 1