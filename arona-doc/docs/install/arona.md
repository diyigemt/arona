# 安装arona

1. 由于图片生成使用到了Java的图形库，如果在Linux环境下部署，需要安装额外的包，以下仅给出apt的安装命令，其他Linux发行版请自行搜索对应包的安装

 ```shell
 sudo apt update
 sudo apt install libxrender-dev
 sudo apt install fontconfig
 sudo fc-cache --force
 ```

2. 在[releases](https://github.com/diyigemt/arona/releases)下载最新版本的jar包并放入mirai-console的`plugins`目录下


3. 编辑`config/Console`目录下的`PluginDependencies.yml`文件，在`repoLoc`下加入一行

`  - 'https://maven.pkg.jetbrains.space/public/p/compose/dev'`

修改后的文件内容大致如下

```yaml
# 远程仓库, 如无必要无需修改
repoLoc: 
  - 'https://maven.aliyun.com/repository/central'
  - 'https://repo1.maven.org/maven2/'
  - 'https://maven.pkg.jetbrains.space/public/p/compose/dev'
```

注意`-`有两个前导空格和一个后空格，如果没有修改过这个文件，建议直接复制上面的覆盖

4. 启动mirai-console，等到显示如下字样后退出：

 ```bash
 yyyy-MM-dd HH:mm:ss I/arona: arona loaded
 yyyy-MM-dd HH:mm:ss I/arona: arona gacha module init success.
 yyyy-MM-dd HH:mm:ss I/ba-activity-pusher: 别名配置更新成功
 yyyy-MM-dd HH:mm:ss I/ba-activity-pusher: 中文字体初始化成功
 yyyy-MM-dd HH:mm:ss I/ba-activity-pusher: Source: STUDENT from GitHub already up to date.
 yyyy-MM-dd HH:mm:ss I/ba-activity-pusher: Source: LOCALIZATION from GitHub already up to date.
 yyyy-MM-dd HH:mm:ss I/ba-activity-pusher: Source: RAID from GitHub already up to date.
 yyyy-MM-dd HH:mm:ss I/ba-activity-pusher: Source: COMMON from GitHub already up to date.
 ```

当然由于网络原因可能并没有最后这四项，不过并不影响使用。

::: info

由于依赖比较神奇的关系，第一次启动会大段报错，下载依赖完成后不会再报错，如果再报错请开个issue反馈

:::

5. 在[releases](https://github.com/diyigemt/arona/releases)下载最新版本的`arona.db`的数据库文件，替换掉`data/net.diyigemt.arona/arona.db`

6. 在`config/net.diyigemt.arona/`文件夹下根据自己的喜好编辑arona的配置文件，具体内容将会在下一节解释

## 初始化

arona的运行依赖`chat-command`插件，因此在使用arona的指令之前，需要在mirai-console的控制台中给予指令使用权限。

arona一共提供了如下的指令：

| 指令权限组                                       | 作用域 | 内置权限控制    | 作用             |
|---------------------------------------------|-----|-----------|----------------|
| net.diyigemt.arona:command.active           | 所有  | 好友/陌生人/群员 | 获取国际服/日服活动状态   |
| net.diyigemt.arona:command.gacha            | 所有  | 管理员       | 配置抽卡参数         |
| net.diyigemt.arona:command.gacha_one        | 仅限群 | 群员        | 单抽             |
| net.diyigemt.arona:command.gacha_multi      | 仅限群 | 群员        | 十连             |
| net.diyigemt.arona:command.gacha_dog        | 仅限群 | 群员        | 查看pickup最小抽取记录 |
| net.diyigemt.arona:command.gacha_history    | 仅限群 | 群员        | 查看抽卡记录         |
| net.diyigemt.arona:command.hentai           | 所有  | 管理员       | 配置发情关键词回复      |
| net.diyigemt.arona:command.config           | 所有  | 管理员       | 配置个服务的开关       |
| net.diyigemt.arona:command.tarot            | 所有  | 好友/陌生人/群员 | 抽一张塔罗牌         |
| net.diyigemt.arona:command.call_me          | 仅限群 | 群员        | 设置自己的昵称        |
| net.diyigemt.arona:command.trainer          | 所有  | 群员        | 查看主线地图和学生攻略    |
| net.diyigemt.arona:command.game_name        | 所有  | 群员        | 让arona记住你的游戏名  |
| net.diyigemt.arona:command.game_name_search | 所有  | 群员        | 模糊查询游戏名对应的群友   |

**注意**，以上语境中的"管理员"指arona主配置文件中`managerGroup`指定的管理员，并不是qq群的管理员

一些解释：

1. 作用域(所有)、权限控制(好友/陌生人/群员)指无论通过何种方法向arona发送信息并被其识别，指令即可触发。

换句话说，你私聊arona发送`/活动 jp`或者在群里发送都能得到响应

2. 作用域(所有)、权限控制(管理员)指无论通过何种方法向arona发送信息并被其识别，指令即可触发，前提是你位于`arona.yml`文件中的`managerGroup`字段中
3. 作用域(仅限群)指只有在群中发送的指令才会被arona响应

因为本插件的指令依附于mirai-console，必须先在console其中给予指令的执行权限才能在群聊中使用指令，因此如果你不是很了解mirai-console的权限管理机制，你可以直接在mirai-console的界面中运行以下命令来直接激活arona的对应指令权限：

```bash
/permission add * net.diyigemt.arona:command.active
/permission add * net.diyigemt.arona:command.gacha
/permission add * net.diyigemt.arona:command.gacha_one
/permission add * net.diyigemt.arona:command.gacha_multi
/permission add * net.diyigemt.arona:command.gacha_dog
/permission add * net.diyigemt.arona:command.gacha_history
/permission add * net.diyigemt.arona:command.hentai
/permission add * net.diyigemt.arona:command.config
/permission add * net.diyigemt.arona:command.tarot
/permission add * net.diyigemt.arona:command.call_me
/permission add * net.diyigemt.arona:command.trainer
/permission add * net.diyigemt.arona:command.game_name
/permission add * net.diyigemt.arona:command.game_name_search
```

如果你想更精确的控制指令的具体权限，需要修改上边指令中`*`的对应内容，可以参考[mirai官方文档](https://docs.mirai.mamoe.net/console/Permissions.html#%E5%AD%97%E7%AC%A6%E4%B8%B2%E8%A1%A8%E7%A4%BA)，在此只给出简短示例

我想让所有人都能使用`trainer`(/攻略)指令

```shell
/permission add * net.diyigemt.arona:command.trainer
```

我想只给群`114513`的群友使用`game_name`(/游戏名)指令

```shell
/permission add g114513 net.diyigemt.arona:command.game_name
```

我想让除了群`114514`以外的群友都能使用`trainer`(/攻略)指令

```shell
/permission add g* net.diyigemt.arona:command.trainer
/permission remove g114514 net.diyigemt.arona:command.trainer
```

你是不是以为是上边那两条指令？很遗憾，`mirai-console`不支持这么细粒度的控制，你只能一个个把允许的群使用`/permission add g<群号> net.diyigemt.arona:command.game_name`添加好

特别的，为了简化权限控制(毕竟一上来还得配权限挺麻烦的，自己组包开mc服的服主应该有同感)，除了`mirai-console`自带的权限控制外，`arona`自己还有一套简单的权限控制逻辑，就和[初始化](#初始化)开头说的一样。