from tools import post_image_to_remote, update_image, update_image_from_api

base_folder = "/some/"

# 更新杂图

if __name__ == '__main__':
    update_image_from_api(base_folder, type=3)
    post_image_to_remote(base_folder)
    