from tools import confirm_action, post_data, post_image_to_remote, update_image_from_api
base_folder = "chapter_map"

# 更新main_map图片文件夹下的图片

if __name__ == '__main__':
    image_dict = update_image_from_api(base_folder, type=2)
    if len(image_dict) == 0:
        print("empty")
        exit(0)
    # 信息收集完成
    print(list(map(lambda item: item["name"], image_dict)))
    if not confirm_action():
        exit(0)
    # 提交到后端进行处理
    post_image_to_remote(base_folder)
    post_data("imageUpdate", image_dict)
