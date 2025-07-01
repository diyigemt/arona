from tools import confirm_action, post_data, post_image_to_remote, update_image, update_image_from_api

base_folder = "some"

# 更新杂图
# tencentcloud-sdk-python-common==3.0.754

if __name__ == '__main__':
    image_dict = update_image_from_api(base_folder, type=3)
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
