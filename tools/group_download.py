from tools import draw_image

images = [
{
    "name": "日服人权.png",
    "path": "./image/some/",
    "url": "https://i0.hdslb.com/bfs/article/fdd24d627d9e19c08744de5fb35a10a2b3a4f03b.png@942w_534h_progressive.webp",
},
{
    "name": "国际服人权.png",
    "path": "./image/some/",
    "url": "https://i0.hdslb.com/bfs/article/18002a78313399cb799572ab0350714b1bf54c66.png@942w_470h_progressive.webp",
}
# ,{
#     "name": "纱织.png",
#     "path": "./image/student_rank/",
#     "url": "https://i0.hdslb.com/bfs/new_dyn/473218dca85027dbbe763eb3b19d31d9425535005.png@1295w.webp",
# }
]

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