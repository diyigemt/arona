from tools import draw_image

images = [{
    "name": "火力演习.png",
    "path": "./image/some/",
    "url": "https://i0.hdslb.com/bfs/new_dyn/0a5afeff2c8108817be5d62472c8ccf1425535005.png@1295w.webp",
}, {
    "name": "日服火力演习.png",
    "path": "./image/some/",
    "url": "https://i0.hdslb.com/bfs/new_dyn/67bffb5b62399cb0739cab9d1aea05a5425535005.png@1295w.webp",
}]

if __name__ == "__main__":
    count = 1
    total = len(images)
    for item in images:
        print("%d/%d" % (count, total))
        name = item["name"]
        path = item["path"]
        url = item["url"].split("@")[0]
        draw_image(url, name, path)
        count += 1