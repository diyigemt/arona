# 登录Bot

::: warning

本节建议在具有可视化环境的操作系统下进行操作

本节具有时效性，最新的登陆方法可在[mirai官方论坛中查看](https://mirai.mamoe.net/)

:::

::: info

所有操作都会在mcl的安装目录下进行，因此所有目录地址都是相对于mcl安装目录的地址

:::

## 配置mirai-console的自动登录

这个步骤不是必须的，但是配置好后可以让你省掉几次操作，即使不打算配置，看完也有用处

用你喜欢的任何一个能够打开并编辑纯文本文件的编辑器打开位于`config/Console`目录下的`AutoLogin.yml`文件

```yaml:line-numbers {3,8,15}
accounts: 
  - # 账号, 现只支持 QQ 数字账号
    account: 123456
    password: 
      # 密码种类, 可选 PLAIN 或 MD5
      kind: PLAIN
      # 密码内容, PLAIN 时为密码文本, MD5 时为 16 进制
      value: pwd
    # 账号配置. 可用配置列表 (注意大小写):
    # "protocol": "ANDROID_PHONE" / "ANDROID_PAD" / "ANDROID_WATCH" / "MACOS" / "IPAD"
    # "device": "device.json"
    # "enable": true
    # "heartbeatStrategy": "STAT_HB" / "REGISTER" / "NONE"
    configuration: 
      protocol: ANDROID_PHONE
      device: device.json
      enable: true
      heartbeatStrategy: STAT_HB
```

修改位于第3、8和15行的内容即可

特别需要**注意第15行**的内容，它会深刻影响接下来的步骤<a id="protocol" />

登录协议的不同**有可能会影响**插件的功能，比如戳一戳事件只在ANDROID_PHONE协议下生效

对于arona来说，如果不使用ANDROID_PHONE协议登录，将无法使用戳一戳功能

::: warning

虽然ANDROID_PHONE的协议是最全的，但是tx对这个协议的检测力度也是最强的

如果没有特殊需要，建议使用非ANDROID_PHONE协议登录，比如ANDROID_WATCH、MACOS协议等

:::

## 安装必要的插件

截至2023-4-11，能让bot顺利登录的插件一共有3个

[mirai-device-generator](https://github.com/cssxsh/mirai-device-generator)

[fix-protocol-version](https://github.com/cssxsh/fix-protocol-version)

[mirai-login-solver-sakura](https://github.com/KasukuSakura/mirai-login-solver-sakura)

分别去release下载最新的后缀名为`.mirai2.jar`的文件放到`plugins`文件夹里即可

部分插件也有通过mcl安装的方法，具体见它们各自的readme

## 登录bot账号

如果你有一台**真实的**Android手机，并且能够在这台手机上的官方**QQ客户端**成功登录bot账号，你可以直接看[这一节](#Aoki)

你即将看到安装过程最麻烦的一节，做好准备

在mcl安装目录下打开控制台窗口，输入`./mcl`启动mirai-console，等待它初始化完成

初始化完成触发自动登录后，你应该会看到这个窗口

<img src="" alt="">

如果你没有配置自动登录，可以使用下面的命令手动执行登录

```bash
/login q号 密码 协议类型
```

协议类型指[这里介绍的内容](#protocol)

## 使用Aoki登录<a id="Aoki" />

Aoki的[主仓库地址在这里](https://github.com/MrXiaoM/Aoki)

他整个流程在readme里说得很清楚了，在此不再赘述

拿到登录产生的bots文件夹中以bot账号命名的文件夹后，将其覆盖到mcl安装目录下的`bots`文件夹中，启动mcl即可完成bot账号的登录

::: warning

如果配置了自动登录，自动登录的协议需要与你在AokiApp里设置的一致

如果是手动登录，protocol的设置也应与AokiApp里设置的一致

:::
