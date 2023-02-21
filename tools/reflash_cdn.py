# -*- coding: utf-8 -*-
# Copyright 2017-2021 Tencent Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from tencentcloud.common.common_client import CommonClient
from tencentcloud.common import credential
from tencentcloud.common.exception.tencent_cloud_sdk_exception import TencentCloudSDKException
from tencentcloud.common.profile.client_profile import ClientProfile
from tencentcloud.common.profile.http_profile import HttpProfile
import getpass
import os
# required pip install --upgrade tencentcloud-sdk-python-common==3.0.754  /// 注意!!!! 必须是这个版本, 新版本有问题

def purgeFiles(paths: list[str]):
    if len(paths) == 0:
        return
    try:
        if os.path.exists(r"C:\Users\%s\.ssh\tencent" % getpass.getuser()):
            with open(r"C:\Users\%s\.ssh\tencent" % getpass.getuser(), "r") as f:
                pwd = f.readlines()
        id = pwd[0].replace("\n", "")
        key = pwd[1].replace("\n", "")
        cred = credential.Credential(id,key)

        httpProfile = HttpProfile()
        # 域名首段必须和下文中CommonClient初始化的产品名严格匹配
        httpProfile.endpoint = "cdn.tencentcloudapi.com"
        clientProfile = ClientProfile()
        clientProfile.httpProfile = httpProfile

        # common client方法支持指定header，如 X-TC-TraceId、X-TC-Canary
        # headers = {
        #     "X-TC-TraceId": "ffe0c072-8a5d-4e17-8887-a8a60252abca"
        # }

        # 实例化要请求的common client对象，clientProfile是可选的。
        common_client = CommonClient("PurgeUrlsCache", '2018-06-06', cred, "", profile=clientProfile)
        # 接口参数作为json字典传入，得到的输出也是json字典，请求失败将抛出异常，headers为可选参数
        print(common_client.call_json("PurgeUrlsCache", {
            "Urls": paths,
            "UrlEncode": True
            }))
    except TencentCloudSDKException as err:
        print(err)

if __name__ == "__main__":
    purgeFiles(paths)