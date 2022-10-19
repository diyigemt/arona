from functools import reduce
import os
import numpy as np
from PIL import ImageDraw, ImageFont, Image
img_folder = "image/history"
base_folder = "/some/"
col = 3
font_size = 28
col_gap = 5
row_gap = int(font_size / 2)
padding = 10
fnt = ImageFont.truetype('C:\Windows\Fonts\msyh.ttc', font_size)
jp_str = "日服"
en_str = "国际服"
other_str = "其他"
# 总力战
DECISIVE_BATTLE = ["HOD", "大蛇", "黑白", "黑白", "寿司", "眼球", "主教"]
# 生成杂图帮助

def a(arr: list[str]):
    res = []
    tmp_name = []
    index_i = 0
    for name in arr:
        tmp_name.append(name)
        index_i += 1
        if index_i >= col:
            res.append(tmp_name)
            tmp_name = []
            index_i = 0
    if index_i < col and index_i != 0:
        # 补全空行
        for i in range(0, col - index_i):
            tmp_name.append("")
        res.append(tmp_name)
    return res

if __name__ == '__main__':
    index = 0
    jp_list = []
    en_list = []
    other_list = []
    battle_list = []
    for file in os.listdir(img_folder + base_folder):
        file_names = file.replace(".png", "").replace(".jpg", "").split("_")
        file_name = file_names[np.argmax(file_names)]
        if file_name.find(jp_str) != -1:
            jp_list.append(file_name)
        elif file_name.find(en_str) != -1:
            en_list.append(file_name)
        elif file_name in DECISIVE_BATTLE:
            battle_list.append(file_name)
        else:
            other_list.append(file_name)
        index += 1
    jp_list.sort()
    en_list.sort()
    other_list.sort()
    battle_list.sort()
    jp_list = a(jp_list)
    en_list = a(en_list)
    other_list = a(other_list)
    battle_list = a(battle_list)

    draw_list = [jp_list, en_list, other_list, battle_list]

    name_list = jp_list + en_list + other_list + battle_list
    # 获取每一列文字偏移量  第一列为0
    max_row_width = []
    for i in range(0, len(name_list[0])):
        # 获取每列自己的最大宽度加上每列间隔
        max_row_width.append(np.max(list(map(lambda list: len(list[i]) + col_gap, name_list))))
    max_row_width[0] = 0
    max_row_width = np.cumsum(np.asarray(max_row_width))
    # 通过最后一列的偏移量和最后一列的最大长度获取整张图片的宽度
    last_col_width = np.max(list(map(lambda name: len(name[col - 1]), name_list)))
    width = int((last_col_width + max_row_width[col - 1]) * font_size + padding * 2)

    max_row = len(name_list) + 3

    # 创建图片
    img = Image.new('RGB', (width, max_row * font_size + max_row * row_gap), color='white')
    draw = ImageDraw.Draw(img)
    cur_row = 0

    # 日服、国际服以及其他分开展示

    for type in draw_list:
        for outer in type:
            for i in range(0, len(outer)):
                draw.text((padding + max_row_width[i] * font_size, cur_row * font_size + row_gap * (cur_row)), outer[i], font=fnt, fill=(0,0,0))
            cur_row += 1
        cur_row += 1


    # draw.text((padding + (width - len(jp_str) * font_size) / 2, cur_row * font_size + row_gap * (cur_row)), jp_str, font=fnt, fill=(0,0,0))
    # cur_row += 1

    # for outer in jp_list:
    #     for i in range(0, len(outer)):
    #         draw.text((padding + max_row_width[i] * font_size, cur_row * font_size + row_gap * (cur_row)), outer[i], font=fnt, fill=(0,0,0))
    #     cur_row += 1

    # draw.text((padding + (width - len(en_str) * font_size) / 2, cur_row * font_size + row_gap * (cur_row)), en_str, font=fnt, fill=(0,0,0))
    # cur_row += 1

    # for outer in en_list:
    #     for i in range(0, len(outer)):
    #         draw.text((padding + max_row_width[i] * font_size, cur_row * font_size + row_gap * (cur_row)), outer[i], font=fnt, fill=(0,0,0))
    #     cur_row += 1

    # draw.text((padding + (width - len(other_str) * font_size) / 2, cur_row * font_size + row_gap * (cur_row)), other_str, font=fnt, fill=(0,0,0))
    # cur_row += 1

    # for outer in other_list:
    #     for i in range(0, len(outer)):
    #         draw.text((padding + max_row_width[i] * font_size, cur_row * font_size + row_gap * (cur_row)), outer[i], font=fnt, fill=(0,0,0))
    #     cur_row += 1

    img.save('image' + base_folder + "杂图.png")
    print("成功! %d" % index) 