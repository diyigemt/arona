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
# 生成杂图帮助

if __name__ == '__main__':
    index = 0
    index_i = 0
    name_list = []
    tmp_name = []
    for file in os.listdir(img_folder + base_folder):
        file_names = file.replace(".png", "").replace(".jpg", "").split("_")
        file_name = file_names[np.argmax(file_names)]
        tmp_name.append(file_name)
        index_i += 1
        if index_i >= col:
            name_list.append(tmp_name)
            tmp_name = []
            index_i = 0
        index += 1
    if index_i < col and index_i != 0:
        # 补全空行
        for i in range(0, col - index_i):
            tmp_name.append("")
        name_list.append(tmp_name)

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
    img = Image.new('RGB', (width, len(name_list) * font_size + len(name_list) * row_gap), color='white')
    draw = ImageDraw.Draw(img)
    cur_row = 0

    for outer in name_list:
        for i in range(0, len(outer)):
            draw.text((0 + padding + max_row_width[i] * font_size, cur_row * font_size + row_gap * (cur_row )), outer[i], font=fnt, fill=(0,0,0))
        cur_row += 1
    img.save('image' + base_folder + "杂图.png")
    print("成功! %d" % index) 