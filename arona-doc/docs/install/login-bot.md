# 登录Bot

::: warning

本节建议在本地且有可视化环境的操作系统下进行操作

本节具有时效性，最新的登陆方法可在[mirai官方论坛中查看](https://mirai.mamoe.net/)

请确保用来登录的bot账号已经绑定了手机号

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

::: warning

本节一定要在本地进行操作，如果你非要直接在远程服务器上玩，我觉得你应该不需要看这个教程

:::

你即将看到安装过程最麻烦的一节，做好准备，整个流程大概如下

<img src="/image/install/flow_chart.webp" alt="验证流程">

::: danger

验证链接是有时效性的，建议先往下看完再回过头开始

:::

在mcl安装目录下打开控制台窗口，输入`./mcl`启动mirai-console，等待它初始化完成

初始化完成触发自动登录后，你应该会看到这个窗口

<img src="/image/install/slider_captcha.webp" alt="滑动验证"/>

如果你没有配置自动登录，可以使用下面的命令手动执行登录

```bash
/login q号 密码 协议类型
```

协议类型指[这里介绍的内容](#protocol)

你有两个选择来完成滑动验证码认证：

 - [使用浏览器的调试功能完成](#browser)
 - [使用mirai-login-solver-sakura配套的Android apk完成](#sakura)

无论你使用那种方法，确保用来完成滑动验证的设备和运行mcl的设备在同一个局域网内

说人话就是如果你用Windows开着mcl，那就继续用这台Windows的浏览器来完成滑动验证；

如果你打算用Android apk，那就连上你家wifi

## 使用浏览器的调试功能完成滑动验证<a id="browser" />

如果要使用浏览器完成滑动验证，确保浏览器和mcl在同一个局域网内

建议使用现代浏览器(Chrome、FireFox、Edge等)来完成

回到上一步的弹框页面，复制`url`字段的内容，它应该长这样

```bash
https://ssl.captcha.qq.com/template/wireless_mqq_captcha.html?style=simple&*************************&clientype=1&apptype=2
```

打开浏览器，并按下键盘上的F12打开浏览器自带的开发者工具

切换到`网络`选项卡，如果是英文的，它应该叫`Network`

<img src="/image/install/network.webp" alt="网络"/>

将复制的地址粘贴到浏览器当前页面的地址栏按下回车访问，不出意外你应该能看到下面这个页面

<img src="/image/install/slider_do.webp" alt="验证界面"/>

同时开发者工具也会刷出一堆东西，点击下图圈起来的这个按钮清空记录方便接下来的操作，不清空也行，你喜欢就好

<img src="/image/install/network_clear.webp" alt="清空网络"/>

回到浏览器界面，手动完成人机验证后回到开发者工具界面

在开发者工具`网络`一栏的记录中从下往上找到一条叫`cap_union_new_verify`的记录，点它查看详情

<img src="/image/install/slider_success.webp" alt="验证成功"/>

在右边的界面中选择`预览`，英文应该是`Preview`

找到叫做`token`的字段，复制它后面的值，**不包括引号**

<img src="/image/install/slider_content.webp" alt="cap_union_new_verify"/>

复制出来的东西应该长这样，再强调一遍，**不包括引号**

```bash
t03FHeRLG6F-JmIu7tQon3Bx8BWGJoRYFpS1KEI002qu3vDCfAxpBAh1eSm2LMuXf1WG1TJ9_GIYjUB75KyvTXG2sCgBC1wnjV8zNLMlY4zZFrRefZjOLndX9L5IGP_whcYOGmDUmyxTq0*
```

把复制出来的这段东西粘贴回mcl的滑动验证界面的`ticket`，按下确定，完成滑动验证的过程

<img src="/image/install/slider_commit.webp" alt="粘贴回去" />

在完成滑动验证后，会弹窗设备锁验证或者短信验证，需要bot账号绑定了手机号或者打开设备锁

<img src="/image/install/slider_second_verify.webp" alt="粘贴回去" />

这里选择短信验证，确保bot账号已经绑定了手机号，发送并回填验证码即可

<img src="/image/install/slider_sms.webp" alt="短信验证" />

最后在控制台看到这个，整个bot登录流程结束

<img src="/image/install/success.webp" alt="登录成功"/>

## 使用mirai-login-solver-sakura配套的Android apk完成滑动验证<a id="sakura" />

这个方法需要你有一台Android机并且与mcl**处于同一个局域网下**，对于大部分家庭环境就是运行mcl的台式机插的网线和Android机连接的wifi都是同一个路由

对于复杂的网络环境，推荐[使用浏览器的调试功能完成](#browser)，或者既然你都能布置出这个环境了，那应该不用我这个外行来教你`处于同一个局域网下`要怎么操作

首先在mirai-login-solver-sakura的release下载和安装在mcl里插件同一个release下的apk包并安装

<img src="/image/install/apk.webp" alt="apk包" />

回到最开始mcl弹出的滑动验证窗口，选择`use SakuraCaptchaHelper`

<img src="/image/install/slider_captcha.webp" alt="使用apk解决">

接下来的流程引导mirai-login-solver-sakura已经做的足够好了

## 使用Aoki登录<a id="Aoki" />

Aoki的[主仓库地址在这里](https://github.com/MrXiaoM/Aoki)

他整个流程在readme里说得很清楚了，在此不再赘述

拿到登录产生的bots文件夹中以bot账号命名的文件夹后，将其覆盖到mcl安装目录下的`bots`文件夹中，启动mcl即可完成bot账号的登录

::: warning

如果配置了自动登录，自动登录的协议需要与你在AokiApp里设置的一致

如果是手动登录，protocol的设置也应与AokiApp里设置的一致

:::

## bots文件夹

mcl安装目录下的`bots`文件夹里保存着bot的快速登录信息

在**完成**一次滑动验证并成功登录后，可以将里面按bot账号命名的文件夹保存下来，

<img src="/image/install/bots.webp" alt="bots文件夹" />

之后在其他mcl环境将bot文件夹放回`bots`文件夹，即可不用滑动验证快速登录bot

**注意**，在其他环境下使用bot文件夹快速登录时，要**确保**配置的登录协议与产生bot文件夹时配置的协议一致

## 登录过程中遇到的错误

在完成滑动验证后或者使用bot文件夹快速登录时，有可能会遇到类似下面的情况

```bash {2}
net.mamoe.mirai.network.WrongPasswordException: 
    Error(bot=Bot(*********), code=237, title=安全提醒, message=当前网络不稳定，登录失败。推荐使用常用设备或通过手机号登录。,errorInfo=)
  at net.mamoe.mirai.internal.network.components.SsoProcessorImpl$slowLoginImpl.doLogin(SsoProcessor.kt:450)
  at net.mamoe.mirai.internal.network.components.SsoProcessorImplsSlowLoginImpl$doLogin$1.invokeSuspend(SsoProcessor.kt)
  at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
  at kotlinx.coroutines.internal.ScopeCoroutine.afterResume(Scopes.kt:33)
  at kotlinx.coroutines.AbstractCoroutine.resumeWith(AbstractCoroutine.kt:102)
  at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumewithContinuationImpl.kt:46)
...5 more
```

code常见的有235、237、238、45等

出现这种情况时，**需要**将`bots`文件夹下当前bot账号对应的文件夹删除，修改自动登录的协议后再走一遍滑动验证的流程

推荐使用经过`fix-protocol-version`插件修复后的协议登录，即`ANDROID_PAD`、`ANDROID_WATCH`、`MACOS`和`IPAD`这几个协议

**不推荐**使用`ANDROID_PHONE`这个协议登录，tx对这个协议的检测最严

如果不论怎么改协议还是遇到这些错误，建议考虑一下是不是bot被tx拉黑了，换个号吧
