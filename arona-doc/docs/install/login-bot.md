# 登录Bot

::: warning

本节建议在本地且有可视化环境的操作系统下进行操作

本节具有时效性(最后更新时间2023-07-14)，最新的登陆方法可在[mirai官方论坛中查看](https://mirai.mamoe.net/)

请确保用来登录的bot账号已经绑定了手机号

:::

::: info

所有操作都会在mcl的安装目录下进行，因此所有目录地址都是相对于mcl安装目录的地址

:::

::: danger

关于mirai-console的最新问题，最好先看一下[无法登陆的临时解决方案](https://mirai.mamoe.net/topic/223/%E6%97%A0%E6%B3%95%E7%99%BB%E5%BD%95%E7%9A%84%E4%B8%B4%E6%97%B6%E5%A4%84%E7%90%86%E6%96%B9%E6%A1%88)

:::

## 安装必要的插件

截至2023-4-11，能让bot顺利登录的插件一共有4个

[mirai-device-generator](https://github.com/cssxsh/mirai-device-generator)

[fix-protocol-version](https://github.com/cssxsh/fix-protocol-version)

[KawaiiMiku](https://github.com/MrXiaoM/KawaiiMiku)

[mirai-login-solver-sakura](https://github.com/KasukuSakura/mirai-login-solver-sakura)

分别去release下载最新的后缀名为`.mirai2.jar`的文件放到`plugins`文件夹里即可

部分插件也有通过mcl安装的方法，具体见它们各自的readme

其中`KawaiiMiku`和`fix-protocol-version`最新版本互相冲突，需要在以下两种组合中安装其中一个

1.  [KawaiiMiku@最新版](https://github.com/MrXiaoM/KawaiiMiku/releases)和[fix-protocol-version@1.8.3](https://github.com/cssxsh/fix-protocol-version/releases/tag/v1.8.3)
2.  只安装[fix-protocol-version@最新版](https://github.com/cssxsh/fix-protocol-version/releases)

## 登录bot账号

::: warning

本节一定要在本地且具有桌面环境的操作系统下进行操作，如果你非要直接在远程服务器上玩，我觉得你应该不需要看这个教程

:::

你即将看到安装过程最麻烦的一节，做好准备

### 前置

自从2023/06/24日后传统验证方法被大规模封禁，需要第三方签名服务才能正常登录，为安全起见，`mirai`本身并不提供协议实现，而是通过支持第三方签名协议，以支持登录。

因此，需要先在本地部署签名服务器，或者去网上找别人部署好公开的服务。

签名服务器跑起来至少需要512MB的内存空间，如果有条件建议自己部署，因为整个签名过程会被签名服务获取，使用公开的签名服务器**可能会**导致账号泄露。

### 部署unidbg-fetch-qsign签名服务

目前(2023/07/14)[unidbg-fetch-qsign](https://github.com/fuqiuluo/unidbg-fetch-qsign)是唯一可行的签名服务，因此接下来进行部署。

1.  在[release](https://github.com/fuqiuluo/unidbg-fetch-qsign/releases)下载最新的压缩包(v1.1.4)；


2.  将压缩包解压到任何一个你喜欢的位置；


3.  编辑`txlib\8.9.63`下的`config.json`，将`server.host`改为`127.0.0.1`，将`server.host`改为`12780`，将`auto_register`改为`true`，改好的应该是这个样子：
```json:line-numbers {3,4,7}
{
  "server": {
    "host": "127.0.0.1",
    "port": 12780
  },
  "key": "114514",
  "auto_register": true,
  "reload_interval": 40,
  "protocol": {
    "qua": "V1_AND_SQ_8.9.63_4194_YYB_D",
    "version": "8.9.63",
    "code": "4194"
  },
  "unidbg": {
    "dynarmic": false,
    "unicorn": true,
    "debug": false
  }
}
```

如果你知道`server.host`和`server.host`代表什么，你也可以将它们改成你喜欢的值。

4.  在解压出来的目录下执行下面的指令

::: code-group

```bash [linux]
bin/unidbg-fetch-qsign --basePath=txlib/8.9.63
```

```sh [windows]
bin/unidbg-fetch-qsign --basePath=txlib/8.9.63
```

:::

看到`Responding at http://127.0.0.1:12780`这行字就算启动成功了

```bash {16}
...
18:23:29.544 [main] DEBUG io.netty.buffer.PooledByteBufAllocator - -Dio.netty.allocator.numDirectArenas: 32
18:23:29.544 [main] DEBUG io.netty.buffer.PooledByteBufAllocator - -Dio.netty.allocator.pageSize: 8192
18:23:29.544 [main] DEBUG io.netty.buffer.PooledByteBufAllocator - -Dio.netty.allocator.maxOrder: 9
18:23:29.544 [main] DEBUG io.netty.buffer.PooledByteBufAllocator - -Dio.netty.allocator.chunkSize: 4194304
18:23:29.544 [main] DEBUG io.netty.buffer.PooledByteBufAllocator - -Dio.netty.allocator.smallCacheSize: 256
18:23:29.544 [main] DEBUG io.netty.buffer.PooledByteBufAllocator - -Dio.netty.allocator.normalCacheSize: 64
18:23:29.545 [main] DEBUG io.netty.buffer.PooledByteBufAllocator - -Dio.netty.allocator.maxCachedBufferCapacity: 32768
18:23:29.545 [main] DEBUG io.netty.buffer.PooledByteBufAllocator - -Dio.netty.allocator.cacheTrimInterval: 8192
18:23:29.545 [main] DEBUG io.netty.buffer.PooledByteBufAllocator - -Dio.netty.allocator.cacheTrimIntervalMillis: 0
18:23:29.545 [main] DEBUG io.netty.buffer.PooledByteBufAllocator - -Dio.netty.allocator.useCacheForAllThreads: false
18:23:29.545 [main] DEBUG io.netty.buffer.PooledByteBufAllocator - -Dio.netty.allocator.maxCachedByteBuffersPerChunk: 1023
18:23:29.552 [main] DEBUG io.netty.buffer.ByteBufUtil - -Dio.netty.allocator.type: pooled
18:23:29.552 [main] DEBUG io.netty.buffer.ByteBufUtil - -Dio.netty.threadLocalDirectBufferSize: 0
18:23:29.552 [main] DEBUG io.netty.buffer.ByteBufUtil - -Dio.netty.maxThreadLocalCharBufferSize: 16384
18:23:29.562 [DefaultDispatcher-worker-1] INFO ktor.application - Responding at http://127.0.0.1:12780
```

::: warning

在无桌面环境的服务器上部署时，建议使用`tmux`或`screen`等软件将服务挂在后台运行，因为除了登录需要签名服务外，**其他任何时候**都可能需要验证签名，因此签名服务需要常开。

:::

### 配置`KawaiiMiku`和`fix-protocol-version`

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



### 使用浏览器的调试功能完成滑动验证<a id="browser" />

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

### 使用mirai-login-solver-sakura配套的Android apk完成滑动验证<a id="sakura" />

这个方法需要你有一台Android机并且与mcl**处于同一个局域网下**，对于大部分家庭环境就是运行mcl的台式机插的网线和Android机连接的wifi都是同一个路由

对于复杂的网络环境，推荐[使用浏览器的调试功能完成](#browser)，或者既然你都能布置出这个环境了，那应该不用我这个外行来教你`处于同一个局域网下`要怎么操作

首先在mirai-login-solver-sakura的release下载和安装在mcl里插件同一个release下的apk包并安装

<img src="/image/install/apk.webp" alt="apk包" />

回到最开始mcl弹出的滑动验证窗口，选择`use SakuraCaptchaHelper`

<img src="/image/install/slider_captcha.webp" alt="使用apk解决">

接下来的流程引导mirai-login-solver-sakura已经做的足够好了

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

## bots文件夹

mcl安装目录下的`bots`文件夹里保存着bot的快速登录信息

在**完成**一次滑动验证并成功登录后，可以将里面按bot账号命名的文件夹保存下来，

<img src="/image/install/bots.webp" alt="bots文件夹" />

之后在其他mcl环境将bot文件夹放回`bots`文件夹，即可不用滑动验证快速登录bot

**注意**，在不同环境下使用bot文件夹快速登录时，要**确保**配置的登录协议与产生bot文件夹时配置的协议一致，**并且**删除以账号(q号)命名的文件夹下的`cache`和`logs`文件夹，只保留`device.json`文件

::: danger

建议在不同ip/地域下迁移bots文件夹后，等待半小时左右再登录，以免因为短时间内两次登录的IP归属地变动较大被风控。

:::

## 将bot部署在远程服务器上



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

出现这种情况时，**需要**将`bots`文件夹下当前bot账号对应的文件夹删除，再走一遍滑动验证的流程

推荐使用经过`fix-protocol-version`插件修复后的协议登录，即`ANDROID_PHONE`、`ANDROID_PAD`这两个协议

**不推荐**使用`ANDROID_WATCH`等协议登录，目前没有任何替代方案

如果不论怎么改协议还是遇到这些错误，建议考虑一下是不是bot被tx拉黑了，换个号吧
