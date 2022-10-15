from tools import draw_image

images = [{
    "name": "日服角色简评.png",
    "path": "./image/some/",
    "url": "https://i0.hdslb.com/bfs/article/cbbbc5d481129f58c7cacf671bfc0772c2cb6bb8.png@942w_711h_progressive.webp",
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