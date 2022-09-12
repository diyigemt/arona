## Mirai-Console安装

Arona是依赖于Mirai-Console的一个插件，并不能独立工作，要使用Arona，必须先安装Mirai-Console。

如果你已经在使用Mirai-Console或对其有一定了解，可以直接看下一节的[Arona安装](#install-2)。

我是懒狗: https://github.com/mamoe/mirai

## Arona安装 <a id="install-2"> </a>

1. 在[releases](https://github.com/diyigemt/arona/releases)下载最新版本的jar包并放入mirai-console的`plugins`目录下；<a id="step1"> </a>

2. 由于图片生成使用到了Java的图形库，如果在Linux环境下部署，需要安装额外的包，以下仅给出apt的安装命令，其他Linux发行版请自行搜索对应包的安装

   ```shell
   sudo apt update
   sudo apt install libxrender-dev
   ```

3. 启动mirai-console，等到显示如下字样后退出：

   ```bash
   2022-07-02 21:27:03 I/arona: arona database init success.
   2022-07-02 21:27:03 I/arona: arona loaded
   2022-07-02 21:27:04 I/arona: arona gacha module init success.
   ```

4. 在[releases](https://github.com/diyigemt/arona/releases)下载最新版本的arona.db的SQLite文件并替换掉`./data/net.diyigemt.arona/arona.db`文件；

5. 在`./config/net.diyigemt.arona/`文件夹下根据自己的喜好编辑arona的配置文件，具体内容将会在下一节解释；

6. 再次运行mirai-console即可享受arona的服务。

**注意**，arona的运行依赖`chat-command`插件，该插件的安装方法与[步骤1](#step1)一致，你可以在[这里](https://github.com/project-mirai/chat-command)找到它的下载链接

**本插件依赖的mirai-console最低版本为2.11.1**

## 初始化注意事项

arona的运行依赖`chat-command`插件，因此在使用arona的指令之前，需要在mirai-console的控制台中给予指令使用权限。

arona一共提供了如下的指令：

| 指令权限组                                  | 作用域 | 内置权限控制     | 作用                     |
| ------------------------------------------- | ------ | ---------------- | ------------------------ |
| net.diyigemt.arona:command.active           | 所有   | 好友/陌生人/群员 | 获取国际服/日服活动状态  |
| net.diyigemt.arona:command.gacha            | 所有   | 管理员           | 配置抽卡参数             |
| net.diyigemt.arona:command.gacha_one        | 仅限群 | 群员             | 单抽                     |
| net.diyigemt.arona:command.gacha_multi      | 仅限群 | 群员             | 十连                     |
| net.diyigemt.arona:command.gacha_dog        | 仅限群 | 群员             | 查看pickup最小抽取记录   |
| net.diyigemt.arona:command.gacha_history    | 仅限群 | 群员             | 查看抽卡记录             |
| net.diyigemt.arona:command.hentai           | 所有   | 管理员           | 配置发情关键词回复       |
| net.diyigemt.arona:command.config           | 所有   | 管理员           | 配置个服务的开关         |
| net.diyigemt.arona:command.tarot            | 所有   | 好友/陌生人/群员 | 抽一张塔罗牌             |
| net.diyigemt.arona:command.call_me          | 仅限群 | 群员             | 设置自己的昵称           |
| net.diyigemt.arona:command.trainer          | 所有   | 群员             | 查看主线地图和学生攻略   |
| net.diyigemt.arona:command.game_name        | 所有   | 群员             | 让arona记住你的游戏名    |
| net.diyigemt.arona:command.game_name_search | 所有   | 群员             | 模糊查询游戏名对应的群友 |

一些解释：

1. 作用域(所有)、权限控制(好友/陌生人/群员)指无论通过何种方法向arona发送信息并被其识别，指令即可触发。

​	换句话说，你私聊arona发送`/活动 jp`或者在群里发送都能得到响应

2. 作用域(所有)、权限控制(管理员)指无论通过何种方法向arona发送信息并被其识别，指令即可触发，前提是你位于`arona.yml`文件中的`managerGroup`字段中
3. 作用域(仅限群)指只有在群中发送的指令才会被arona响应

因为本插件的指令依附于mirai-console，必须现在其中给予指令的执行权限才能在群聊中使用指令，因此如果你不是很了解mirai-console的权限管理机制，你可以直接在mirai-console的界面中运行以下命令来直接激活arona的对应指令权限：

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

## Arona配置

在这: [arona配置](./using.md#config)