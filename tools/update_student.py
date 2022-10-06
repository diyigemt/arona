from tools import update_image_from_api, post_image_to_remote

base_folder = "/student_rank/"


# 更新杂图

if __name__ == '__main__':
    update_image_from_api(base_folder, type=1)
    post_image_to_remote(base_folder)
