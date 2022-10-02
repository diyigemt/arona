from tools import draw_image

images = [{
    "name": "颜艺.png",
    "path": "./image/student_rank/",
    "url": "https://i0.hdslb.com/bfs/new_dyn/801fefd35ed73a7651a4be06ddaa22c7425535005.png",
}, {
    "name": "车白.png",
    "path": "./image/student_rank/",
    "url": "https://i0.hdslb.com/bfs/new_dyn/b2e9d36e975fb8018a86a635041e2ef5425535005.png",
}, {
    "name": "朱音.png",
    "path": "./image/student_rank/",
    "url": "https://i0.hdslb.com/bfs/new_dyn/cfdf6af370fb089f9ff8852a712e665f425535005.png@1295w.webp",
}]

if __name__ == "__main__":
    count = 1
    total = len(images)
    for item in images:
        print("%d/%d" % (count, total))
        name = item["name"]
        path = item["path"]
        url = item["url"]
        draw_image(url, name, path)
        total += 1