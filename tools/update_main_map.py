from tools import post_image_to_remote, update_image_from_api
base_folder = "/chapter_map/"

# 更新main_map图片文件夹下的图片

if __name__ == '__main__':
    update_image_from_api(base_folder, type=2)
    post_image_to_remote(base_folder)