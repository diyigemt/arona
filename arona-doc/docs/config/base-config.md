# 配置文件详解

arona所有配置文件均采用**yaml**文件格式保存，在编辑其内容时请按照`yaml`文件格式进行，如果不确定自己编辑的格式是否正确，可以先学习简单的语法

这个网站也可以帮助你判断编辑后的文件格式是否正确：[bejson](https://www.bejson.com/validators/yaml_editor/)

yaml文件对格式**十分敏感**，在保存之前**建议**使用在线网站**验证**文件内容格式的正确性

::: warning

由于console的特性和我自己的懒惰，配置文件需要在console停止的情况下修改才会在启动后生效

:::

## arona.yml

arona总的配置。

| 键                       | 属性           | 作用                                   |
|:------------------------|--------------|--------------------------------------|
| qq                      | Long         | 指定arona运行在哪个机器人上                     |
| groups                  | List\<Long\> | 指定arona响应哪个群的消息                      |
| managerGroup            | List\<Long\> | 指定具有arona管理权限的qq号                    |
| permissionDeniedMessage | String       | 当不具有管理员的用户尝试执行需要管理权限的指令时的回复消息,为空则不回复 |
| sendOnlineMessage       | Boolean      | 是否发送arona上线消息                        |
| onlineMessage           | String       | 上线消息内容                               |
| sendOfflineMessage      | Boolean      | 是否发送arona下线消息                        |
| offlineMessage          | String       | 下线消息内容                               |
| updateCheckTime         | Int          | 每日检查更新的时间(24小时制)                     |
| endWithSensei           | String       | 名称是否自动带上后缀，默认为"老师"，可以留空              |
| sendStatus              | Boolean      | 是否允许arona收集匿名统计信息(未实装)               |
| uuid                    | String       | 识别id(无需修改)                           |
| remoteCheckInterval     | Int          | 远端操作查询间隔 设置为0表示不开启, 单位是小时            |
| tempMessageIgnoreType   | Enum         | 是否忽略私聊指令                             |

当`tempMessageIgnoreType`值为`NONE`时，该配置不生效, 为`ONLY_SERVICE_GROUP`时会忽略非服务群以外的私聊消息, 为`ALL`时将不会响应私聊消息<a id="ignore-message-type"> </a>

该配置**不影响**`managerGroup`的私聊指令

## arona-service.yml<a id="service-names"> </a>

各功能模块开关的配置。

| 键       | id | 属性      | 作用                  |
|---------|----|---------|---------------------|
| 配置      | 0  | Boolean | 是否开启不停机修改本配置文件内容的功能 |
| 抽卡配置    | 1  | Boolean | 是否开启不停机修改激活的卡池功能    |
| 发情配置    | 2  | Boolean | 是否开启不停机修改发情配置的功能    |
| 活动查询    | 3  | Boolean | 是否开启当前日服/国际服活动查询指令  |
| 抽卡单抽    | 4  | Boolean | 是否开启单抽指令            |
| 抽卡十连    | 5  | Boolean | 是否开启十连指令            |
| 抽卡狗叫查询  | 6  | Boolean | 是否开启抽卡狗叫查询指令        |
| 抽卡历史查询  | 7  | Boolean | 是否开启抽卡历史查询指令        |
| 复读      | 8  | Boolean | 是否开启复读功能            |
| 发情      | 9  | Boolean | 是否开启发情回怼功能          |
| 摸头回复    | 10 | Boolean | 是否开启摸头回复功能          |
| 岁月史书    | 11 | Boolean | 是否开启岁月史书功能(暂时没做)    |
| 活动推送    | 12 | Boolean | 是否开启活动防侠推送功能        |
| nga图楼推送 | 13 | Boolean | 是否开启NGA图楼推送功能       |
| 自动更新检查  | 14 | Boolean | 是否开启每日更新检查功能        |
| 合并转发    | 15 | Boolean | 是否开启多图合并转发功能(暂时没做)  |
| 塔罗牌     | 16 | Boolean | 是否开启塔罗牌指令           |
| 自定义昵称   | 18 | Boolean | 是否启用自定义昵称           |
| 数据同步服务  | 19 | Boolean | 是否自动从SchaleDB同步活动消息 |
| 地图与学生攻略 | 20 | Boolean | 是否启用地图攻略功能          |
| 游戏名记录   | 21 | Boolean | 是否启用记录游戏名与群名对应关系的功能 |
| 游戏名反查   | 22 | Boolean | 是否启用查询游戏名对应的群名的功能   |

## arona-gacha.yml

抽卡模块配置。

**注意**1、2、3星总出率加起来需要达到100%，2、3星限定出率不能超过各自的总出率。

当前激活的池子的设置与数据库有关，将会在[数据库](./database)进行讨论。

| 键               | 属性    | 作用                              |
|-----------------|-------|---------------------------------|
| star1Rate       | Float | 1星总出率百分比                        |
| star2Rate       | Float | 2星总出率百分比                        |
| star3Rate       | Float | 3星总出率百分比                        |
| star2PickupRate | Float | 2星限定出率百分比                       |
| star3PickupRate | Float | 3星限定出率百分比                       |
| activePool      | Int   | 当前激活的池子                         |
| revokeTime      | Int   | 撤回结果信息防止刷屏 撤回时间间隔(单位为秒) 为0表示不撤回 |
| day             | Int   | 保存上一次更新抽卡信息的日期                  |
| limit           | Int   | 每天每人最多抽几次，设置为0表示不限制             |

## arona-notify.yml

防侠通知模块设置。**注意**，时间按24小时计。

除了双倍掉落提醒时间为晚上22点外，防侠提醒会在活动结束前1个小时进行，因为双倍掉落是在晚上3点结束，2点提醒有点阴间。

| 键                            | 属性      | 作用                                                         |
|------------------------------|---------|------------------------------------------------------------|
| enableEveryDay               | Boolean | 是否启用每日防侠提醒功能                                               |
| notifyType                   | String  | 每日提醒类型,可选ALL(提醒所有时段),ONLY_24H(仅提醒24小时内),ONLY_48H(仅提醒48小时内) |
| everyDayHour                 | Boolean | 每日防侠提醒的时间(同时也是每日数据更新时间)                                    |
| enableJP                     | Boolean | 是否启用日服防侠提醒                                                 |
| notifyStringJP               | Int     | 日服防侠提醒开头文字                                                 |
| enableEN                     | Boolean | 是否启用国际服防侠提醒                                                |
| notifyStringEN               | Int     | 日服防侠提醒开头文字                                                 |
| enableCN                     | Boolean | 是否启用国服防侠提醒                                                 |
| notifyStringCN               | Int     | 国服防侠提醒开头文字                                                 |
| defaultActivityCommandServer | Enum    | "/活动"指令的默认目标服务器,可选值为 "JP"、"GLOBAL"和"CN"                    |

例如`defaultActivityCommandServer`配置为`JP`，那么直接执行`/活动`指令也可以得到和执行`/活动 jp`一致的效果<a id="default-activity"> </a>

**不建议**修改`notifyType`和`everyDayHour`的默认配置

## arona-nudge.yml

摸头模块配置。

| 键           | 属性                      | 作用                     |
|-------------|-------------------------|------------------------|
| messageList | List<Data<String, Int>> | 回复消息列表以及权重             |

这是一个配置示例

```yaml
# 回复的消息
messageList: 
  - message: 爬
    weight: 1
  - message: '${teacherName}别戳了>_<'
    weight: 1
```

`${teacherName}`指用户自行通过[叫我指令](../command/manual#call_me)配置的名称

## arona-repeat.yml

复读模块配置。

| 键     | 属性  | 作用                |
|-------|-----|-------------------|
| times | Int | 当一条消息被重复几次后进行一次复读 |

## arona-hentai.yml

发情回怼模块配置。目前只会对消息内容中含有"老婆"或者"老公"字样的消息进行回复。

作用是应对群友互相发情的，可以不用管。

| 键           | 属性                      | 作用         |
|-------------|-------------------------|------------|
| messageList | List<Data<String, Int>> | 回复消息列表以及权重 |
| listen      | List\<Long>             | 监听的群友QQ号   |

这是一个配置示例

```yaml
# 要被骂的id
listen: 
  - 123123123
  - 12312312
# 返回被骂的信息
messageList: 
  - message: 爬
    weight: 1
  - message: 老师大变态❤
    weight: 1
```

## arona-tarot.yml

塔罗牌配置。

| 键      | 属性      | 作用                                   |
|--------|---------|--------------------------------------|
| dayOne | Boolean | 是否每天只能抽一张，为true时一天中同一个人在同一个群中的抽卡结果相同 |

## arona-trainer.yml <a id="other-name-config"> </a>

别名配置。

| 键                   | 属性                                       | 默认值  | 作用                 |
|---------------------|------------------------------------------|------|--------------------|
| tipWhenNull         | Boole                                    | true | 对应图片不存在时是否提示模糊搜索结果 |
| tipRevokeTime       | Int                                      | 10   | 模糊搜索结果撤回时间         |
| tipResponseWaitTime | Int                                      | 10   | 等待用户对模糊搜索结果的响应时间   |
| override            | List<TrainerOverride(type, name, value)> | []   | 覆盖指令`/攻略`提供的参数     |

### 1. tipWhenNull<a id="name-guess-config"> </a>

对应图片不存在时是否提示模糊搜索结果，为true时，若名字不正确且没有模糊搜索结果，则回复"请联系管理员添加"

### 2. tipRevokeTime

模糊搜索结果撤回时间, 设置为0代表不撤回. 单位是秒

### 3.ValueDescription

等待用户对模糊搜索结果的响应时间, 设置为0代表关闭响应. 单位是秒

当tipWhenNull开启且ValueDescription不为0时，效果如下

::: details 太长了

<img src="/image/config/name-guess.png" />

:::

### 4. override

配置本地的攻略字段内容

`type` 字段是一个枚举值，可选的有`IMAGE, RAW, CODE`

`name` 为要覆盖的原始值，当`/攻略` 指令后接的参数与该值一致时执行`type`对应的动作

`value` 为覆盖后的值，根据`type`的不同也有不同的填写要求

`IMAGE`：代表当规则生效时，发送一张`path`指向的图片，其中`path`值的相对目录为`./data/net.diyigemt.arona/image`

配置示例:

```yaml
override:
  - type: IMAGE
    name: '乐'
    value: '/test/乐.gif'
```

代表当指令为`/攻略 乐`时，发送`./data/net.diyigemt.arona/image`文件夹下`test`子文件夹中的`乐.gif`图片。

`RAW`：代表当规则生效时，将`name`替换成`value`继续执行原有逻辑

配置示例:

```yaml
override:
  - type: RAW
    name: 'HOD'
    value: '黑白'
```

代表当指令为`/攻略 HOD`时，发送`黑白`的攻略图片，效果如下

::: details 太长了

<img src="/image/config/override-name2.png" />

:::

`CODE`：代表当规则生效时，将`value`的值看作`mirai-code`反序列化为消息片段并发送

`mirai-code`简单来说就是机器人启动后每收到一条消息在`mirai-console`中的打印值，具体解释可以看[这里](https://docs.mirai.mamoe.net/Messages.html#mirai-%E7%A0%81)

配置示例:

```yaml
override:
  - type: CODE
    name: '黑服'
    value: '南通爬'
```

代表当指令为`/攻略 黑服`时，发送`南通爬`。后期将会支持@对应发送人的功能。

**特别的**，`name`的内容可以为多个值并以**英文**逗号： ","  进行分割，并且会忽略**一切**空格，例如<a id="other-name-config-multi"> </a>

```yaml
override:
  - type: CODE
    name: '黑服,凯撒'
    value: '南通爬'
```

代表当指令为`/攻略 黑服`或者`/攻略 凯撒`时，发送`南通爬`。

多条配置示例

```yaml
override:
  - type: CODE
    name: '黑服,凯撒'
    value: '南通爬'
  - type: RAW
    name: 'HOD'
    value: '黑白'
```

### 5. trainer_config.yml配置详解<a id="other-name-config-2"> </a>

**首先**要明确的一点是，该配置文件位于`./data/net.diyigemt.arona/config/trainer_config.yml`

与config目录下的`./config/net.diyigemt.arona/trainer_config.yml`并**不是同一个**

配置文件采用**yaml**文件格式保存。在编辑其内容时请按照`yaml`文件格式进行，如果不确定自己编辑的格式是否正确，可以先学习简单的语法。这个网站也可以帮助你判断编辑后的文件格式是否正确：[bejson](https://www.bejson.com/validators/yaml_editor/)

配置内容与`config`目录下的内容覆盖关系为该配置文件**>**`config`下的文件

配置内容同上`arona-trainer.yml`，区别在于**仅**包含`override`这个字段，下面是一个配置示例

yaml文件对格式**十分敏感**，在保存之前**建议**使用在线网站**验证**文件内容格式的正确性

```yaml
override:
  - type: RAW
    name: '拉拉响, 啦啦响'
    value: '拉响'
```

在发送`/攻略 拉拉响 `或`/攻略 啦啦响 `时，发送`响(啦啦队)`的攻略信息。

arona会在启动后持续监听该文件的改动，一经保存会立即生效，并且控制台会输出：

```shell
yyyy-MM-dd HH:mm:ss I/ba-activity-pusher: 别名配置更新成功
```

表明配置生效。当文件格式错误时，会显示：

````shell
yyyy-MM-dd HH:mm:ss I/ba-activity-pusher: 序列化别名配置时失败
````

此时新的设置不会生效，而是保持上一次的配置内容。

## nga.yml

NGA图楼推送配置，具体配置方法可以看[下面](#nga-config)

| 键             | 属性                      | 作用                                                             |
|---------------|-------------------------|----------------------------------------------------------------|
| uid           | String                  | 你自己的nga uid                                                    |
| cid           | String                  | 你自己的nga cid                                                    |
| checkInterval | Int                     | 扫描周期，单位min                                                     |
| source        | MAIN / SUB              | 配置nga数据来源，可选MAIN或SUB，防止nga炸了，main是ngabbs，sub是178.现在应该只有178能用了。 |
| watch         | Map<Int, String>        | 监听的nga cid(无须修改)                                               |
| cache         | List<Pair<Int, String>> | 已经推送过的楼层缓存(无须修改)                                               |

## NGA模块配置方法<a id="nga-config"> </a>

uid:填入你自己的nga的uid，可以在这里看到

::: details uid位置

<img src="/image/config/nga-uid.png" />

:::

cid:这个就比较复杂了，首先你需要知道你所使用的浏览器如何查看当前网页使用的cookie内容，这里以chrome为例，其他浏览器请自行百度

::: details 步骤1

<img src="/image/config/nga-cid-1.png" />

:::

::: details 步骤2

<img src="/image/config/nga-cid-2.png" />

:::

::: details 步骤3

<img src="/image/config/nga-cid-3.png" />

:::

将这串复制填入`nga.yml`对应的位置即可

**注意**，在NGA执行`所有已登录的进程登出`操作后这段内容会失效，需要停止console然后更新配置文件至新的内容
