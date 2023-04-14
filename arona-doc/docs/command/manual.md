# 主动触发

## 活动系列

`/活动` 获取默认活动服务器的活动状态，默认服务器配置在[这里看](../config/base-config#default-activity)

`/活动 jp` 获取日服活动状态

`/活动 en` 获取国际服活动状态

`/活动 cn` 获取国服活动状态

## 抽卡系列

`/单抽` 单抽一次

`/十连` 抽一次十连

`/狗叫` 查看当前卡池最低抽出pickup次数排行榜

`/历史` 查看抽卡历史记录/几抽1个3星

## 抽卡配置系列

`/抽卡 setpool <number>`  设置当前池子为数据库中主键为指定的[number]的池子，如果你不知道这是什么意思，你可以看看[这节](#setpool-config)

`/抽卡 reset` 重置当前池子的抽卡记录

`/抽卡 1s <number>` 设置1星出货率

`/抽卡 2s <number>` 设置2星出货率

`/抽卡 3s <number>` 设置3星出货率

`/抽卡 p2s <number>` 设置2星pick up出货率

`/抽卡 p3s <number>` 设置3星pick up出货率

`/抽卡 time <number>` 设置撤回时间

`/抽卡 limit <number>` 设置每日限制次数

`/抽卡 update <number>` 从远端更新池子，具体使用看[这里](#remote-pool-update)

`/抽卡 update2 <number> <name>` 从远端更新池子并重命名，具体使用看[这里](#remote-pool-update)

`/抽卡 list` 查看最近两个池子的配置

**注意** 1、2、3星的出货率支持浮点数且它们的和需要等于100，2、3星pick up出货率不能高于各自的总出货率，否则抽卡功能可能不能正常运行

## 发情系列

`/发情 adds <string> [number]`  为发情添加一条回复语句(string)并指定权重为number

`/发情 add {number | @member}` 添加一个发情的监听对象(群友)，其中number为群友qq号，或者直接@群友

`/发情 remove {number | @member}` 删除一个发情的监听对象(群友)，其中number为群友qq号，或者直接@群友

## 不停机配置系列

其中的string | number可选值可在[这节看到](#service-names)

`/配置 启用 [string | number] ` 根据名字/id启用一个功能模块

`/配置 停用 [string | number] ` 根据名字/id启用一个功能模块

`/配置 状态 [string | number] ` 根据名字/id查询一个功能模块的状态

`/配置 状态 `  查询所有功能模块的状态

## 塔罗牌系列

`/塔罗牌` 抽取一张塔罗牌

`1.0.12`后，结果会同时附上P站[@Shi0n老师](https://www.pixiv.net/users/4150140)绘制的BA版塔罗牌图片

## 昵称系列<a id="call_me"> </a>

`/叫我` 查询自己的昵称

`/叫我 <string>` 将自己的昵称设置为string

其中名字替换将会在 1.2抽卡系列、arona-nudge的messageList->message、塔罗牌中进行替换

比如arona-nudge.yml的配置为：

```yaml
messageList:
  - message: '${teacherName}别戳了>_<'
    weight: 1
```

那么${teacherName}将会被替换为用户设置的昵称（假设为萝莉控，且arona.yml->endWithSensei配置为"老师"），最终结果为"萝莉控老师别戳了>_<"

## 学生与主线地图攻略系列<a id="main-map"> </a>

`/攻略 <string>`查看主线地图走格子或者学生的图文攻略。

其中学生攻略来源于[巴哈姆特@夜喵貓貓咪喵(asaz5566a)](https://wall.gamer.com.tw/user.php?userId=asaz5566a)

大概长这样

<details>
    <summary>H19-3图文攻略:</summary>
    <img src="/image/install/success.webp" alt="" />
</details>

<details>
    <summary>学生攻略:</summary>
    <img src="/image/install/success.webp" alt="" />
</details>
其中`string`内容为1-1至H19-3之间或者学生的名字/黑话(可能收集不全)，如查看主线普通地图5-3的攻略，指令为`/攻略 5-3`；

查看主线困难地图H19-3的攻略，指令为`/攻略 H19-3`；

查看佑香的攻略，指令为`/攻略 佑香`或者`/攻略 没包人`?。

1.0.8版本后，额外增加了其他杂项一图流的攻略，例如

<details>
    <summary>HOD图文攻略:</summary>
    <img src="/image/install/success.webp" alt="" />
</details>

<details>
    <summary>日服学生人权:</summary>
    <img src="/image/install/success.webp" alt="" />
</details>
由于杂图太多而且不好记名字，因此提供指令`/攻略 杂图`显示所有可用的名字列表

<details>
    <summary>杂图列表:</summary>
    <img src="/image/install/success.webp" alt="" />
</details>
1.0.9版本后，额外增加了别名覆写功能，在原有基础上用户可自定义简短的别名方便记忆<a id="other-name"> </a>

<details>
    <summary>示例配置:</summary>
    <img src="/image/install/success.webp" alt="" />
</details>

<details>
    <summary>效果:</summary>
    <img src="/image/install/success.webp" alt="" />
    <img src="/image/install/success.webp" alt="" />
</details>
具体配置可看下面的[配置文件详解](#other-name-config)

1.0.10版本后，额外增加特殊配置文件，位于`./data/net.diyigemt.arona/config/trainer_config.yml`，允许用户在不停止`mirai-console`的情况下修改`/攻略`指令的别名覆写配置。<a id="other-name-runtime"> </a>

具体配置可看下面的[配置文件详解](#other-name-config-2)

1.0.14版本后，额外增加可通过猜你喜欢自动执行指令的功能<a id="name-guess-using"> </a>

<details>
    <summary>效果:</summary>
    <img src="/image/install/success.webp" alt="" />
</details>

具体配置可看下面的[配置文件详解](#name-guess-config)

## 游戏名记录<a id="game-name"> </a>

`/游戏名 string` 将自己的游戏名添加到记录中。

`/[谁叫|谁是] string` 根据提供的字符串模糊查询游戏名包含字符串的群友。

以上命令也可以通过群私聊机器人调用，此时`谁是`指令将只提供游戏内名称并附上对应qq号。

<details>
    <summary>例如:</summary>
    <img src="/image/install/success.webp" alt="" />
</details>
由于是模糊查询的原因，查询结果可能会涉及多个群友，没事把人家@出来也不好，因此1.0.8后查询结果将不会再@。
