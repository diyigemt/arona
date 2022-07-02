## 声明

<h3>一切开发旨在学习，请勿用于非法用途</h3>

- arona 是一款免费且开放源代码的软件，仅供学习和娱乐用途使用。
- arona 不会通过任何方式强制收取费用，或对使用者提出物质条件。
- arona 由整个开源社区维护，并不是属于某个个体的作品，所有贡献者都享有其作品的著作权。

## 许可证

详见 https://github.com/diyigemt/arona/blob/master/LICENSE

arona 继承 [Mirai](https://github.com/mamoe/mirai) 使用 AGPLv3 协议开源。为了整个社区的良性发展，我们强烈建议您做到以下几点：

- 间接接触到 arona 的软件使用 AGPLv3 开源
- **不鼓励，不支持一切商业使用**

请注意，由于种种原因，开发者可能在任何时间**停止更新**或**删除项目**。

### 衍生软件需声明引用

- 若引用 arona 发布的软件包而不修改 arona ，则衍生项目需在描述的任意部位提及使用 arona 。
- 若修改 arona 源代码再发布，或参考 arona 内部实现发布另一个项目，则衍生项目必须在文章首部或 'miraiboot' 相关内容首次出现的位置明确声明来源于本仓库 ([arona](https://github.com/diyigemt/arona))。
- 不得扭曲或隐藏免费且开源的事实。

---

## Statement

<h3>All development is for learning, please do not use it for illegal purposes</h3>

- arona is a free and open source software for learning and entertainment purposes only.
- arona will not compulsorily charge fees or impose material conditions on users in any way.
- arona is maintained by the entire open source community and is not a work belonging to an individual. All contributors enjoy the copyright of their work.

## License

See https://github.com/diyigemt/arona/blob/master/LICENSE for details

arona inherits [Mirai](https://github.com/mamoe/mirai) Open source using AGPLv3 protocol. For the healthy development of the entire community, we strongly recommend that you do the following:

- Software indirectly exposed to arona uses AGPLv3 open source
- **Does not encourage and does not support all commercial use**

Please note that for various reasons, developers may **stop updating** or **deleting** projects at any time.

### Derivative software needs to declare and quote

- If you quote the package released by arona without modifying arona , the derivative project needs to mention miraiboot in any part of the description.
- If the arona source code is modified and then released, or another project is released by referring to arona's internal implementation, the derivative project must be clearly stated in the first part of the article or at the location where 'arona'-related content first appears from this repository ([arona](https://github.com/diyigemt/arona)).
- The fact that it is free and open source must not be distorted or hidden.

## 介绍

arona是基于mirai-console的插件。

作为BA(Blue Archive)的一款群助手，她可以实现以下功能：

1. 抽卡模拟及数据记录；
2. 国际服/日服活动推送以及防侠提醒；
3. 摸头回复。

## 使用方法

以下需要

1. 在[releases](https://github.com/diyigemt/arona/releases)下载最新版本的jar包并放入mirai-console的`plugins`目录下；
2. 启动mirai-console，等到显示如下字样后退出：

```bash
2022-07-02 21:27:03 I/arona: arona database init success.
2022-07-02 21:27:03 I/arona: arona loaded
2022-07-02 21:27:04 I/arona: arona gacha module init success.
```

3. 在[releases](https://github.com/diyigemt/arona/releases)下载最新版本的arona.db的SQLite文件并替换掉`./data/net.diyigemt.arona/arona.db`文件；
4. 在`./config/net.diyigemt.arona/`文件夹下根据自己的喜好编辑arona的配置文件，具体内容将会在下一节解释；
5. 再次运行mirai-console即可享受arona的服务。

**注意**，arona的运行依赖`chat-command`插件，你可以在[这里](https://github.com/project-mirai/chat-command)找到它的下载链接

## 注意事项

arona的运行依赖`chat-command`插件，因此在使用arona的指令之前，需要给予指令使用权限。

arona一共提供了如下的指令：

| 指令权限组                               | 内置权限控制 | 作用                    |
| ---------------------------------------- | ------------ | ----------------------- |
| net.diyigemt.arona:command.active        | 群员         | 获取国际服/日服活动状态 |
| net.diyigemt.arona:command.gacha         | 管理员       | 配置抽卡参数            |
| net.diyigemt.arona:command.gacha_one     | 群员         | 单抽                    |
| net.diyigemt.arona:command.gacha_multi   | 群员         | 十连                    |
| net.diyigemt.arona:command.gacha_dog     | 群员         | 查看pickup最小抽取记录  |
| net.diyigemt.arona:command.gacha_history | 群员         | 查看抽卡记录            |
| net.diyigemt.arona:command.hentai        | 管理员       | 配置发情关键词回复      |

如果你不是很了解mirai-console的权限管理机制，你可以直接在mirai-console的界面中运行以下命令来直接激活arona的响应权限：

```bash
/permission add * net.diyigemt.arona:command.active
/permission add * net.diyigemt.arona:command.gacha
/permission add * net.diyigemt.arona:command.gacha_one
/permission add * net.diyigemt.arona:command.gacha_multi
/permission add * net.diyigemt.arona:command.gacha_dog
/permission add * net.diyigemt.arona:command.gacha_history
/permission add * net.diyigemt.arona:command.hentai
```

## 指令详解

| 指令                             | 别名                               | 作用                                             |
| -------------------------------- | ---------------------------------- | :----------------------------------------------- |
| /活动 en                         | /active en                         | 获取国际服活动状态                               |
| /活动 jp                         | /active jp                         | 获取日服活动状态                                 |
| /单抽                            | /gacha_one                         | 单抽一次                                         |
| /十连                            | /gacha_multi                       | 十连                                             |
| /狗叫                            | /gacha_dog                         | 查看最低抽出pickup次数排行榜                     |
| /历史                            | /gacha_history                     | 查看抽卡历史记录/几抽1个3星                      |
| /抽卡 setpool <number>           | /gacha setpool <number>            | 设置当前池子为数据库中指定的[number]             |
| /抽卡 reset                      | /gacha reset                       | 重置当前池子的抽卡记录                           |
| /发情 adds <string> [number]     | /hentai adds <string> [number]     | 为发情添加一条回复语句(string)并指定权重为number |
| /发情 adds {number \| @member}   | /hentai add {number \| @member}    | 添加一个发情的监听对象(群友)                     |
| /发情 remove {number \| @member} | /hentai remove {number \| @member} | 删除一个发情的监听对象(群友)                     |
| /发情 enable                     | /hentai enable                     | 启用发情监听                                     |
| /发情 disable                    | /hentai disable                    | 停用发情监听                                     |

## 配置文件详解

### 1.arona.yml

## 数据库详解



## 鸣谢

超级课程表

碧蓝档案国际服

mirai

群友