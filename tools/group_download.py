from tools import draw_image

images = [{
    "name": "温泉千夏_温泉夏.png",
    "path": "./image/student_rank/",
    "url": "https://i0.hdslb.com/bfs/album/37fda72d4748a95557efe610397b49c10434cf5d.png@1295w.webp",
}, {
    "name": "茜_朱音_破甲女仆_破防女仆.png",
    "path": "./image/student_rank/",
    "url": "https://i0.hdslb.com/bfs/album/6c844db6ef54d72c5019805869af21b582c3e6ed.png@1295w.webp",
}, {
    "name": "日服角色简评.png",
    "path": "./image/some/",
    "url": "https://i0.hdslb.com/bfs/album/f21c38461a205648ca171531130b319bf2defea4.png@1295w.webp",
}, {
    "name": "泳装伊树菜_泳装泉奈_水忍忍_水忍狐_水泉奈_水小狐狸.png",
    "path": "./image/student_rank/",
    "url": "https://i0.hdslb.com/bfs/new_dyn/187f46e49b589d3f48e97dadb521d089425535005.png@1295w.webp",
}, {
    "name": "泳装静子_水静子_水老板娘.png",
    "path": "./image/student_rank/",
    "url": "https://i0.hdslb.com/bfs/new_dyn/330fe1b437e4c7b2e8a26e9eadd7ef85425535005.png@1295w.webp",
}, {
    "name": "啦啦队响_拉响.png",
    "path": "./image/student_rank/",
    "url": "https://i0.hdslb.com/bfs/new_dyn/081bb570228dc0b51a4b8779c84af637425535005.png@1295w.webp",
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