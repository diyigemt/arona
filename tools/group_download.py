from tools import draw_image

images = [{
    "name": "日服兔子.png",
    "path": "./image/some/",
    "url": "https://i0.hdslb.com/bfs/new_dyn/279529219ad713b93e471bbd4968d705425535005.png",
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