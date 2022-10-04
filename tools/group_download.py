from tools import draw_image

images = [{
    "name": "国际服活动.png",
    "path": "./image/some/",
    "url": "https://i0.hdslb.com/bfs/article/0e01adfed0a8773120c61925712ba65f989ece00.jpg@942w_488h_progressive.webp",
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