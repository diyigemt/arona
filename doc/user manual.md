## 用户手册(管理侧)

### 机器人交互

在webui存在的情况下，使用纯指令形式对机器人进行配置似乎有些多余，更何况复杂的配置情况下可视化显然比纯指令配置更有优势。

但是仍不可否认纯指令配置比可视化配置更方便快捷，毕竟直接敲字比你开个网页更快，而且受制于机器人本身的存在形式，我也不可能在聊天框强行发一个按钮让你点一下就可以确认啥的。

2.0版本的配置文件进行了大改，绝大部分配置都支持精确到群，所以，你让我怎么办嘛。总不可能每次都问你要应用到哪个群吧。

综上，指令形式的配置仍然保留，但是部分功能只能在web端进行配置。

#### 0. 一些语义

| 标识   | 语义               | 例                             | 可能的合法值           |
| ------ | ------------------ | ------------------------------ | ---------------------- |
| string | 替换为具体的字符串 |                                |                        |
| number | 替换为具体的数值   |                                |                        |
| <>     | 里面的值是必须的   | /arona_gacha setpool \<number> | /arona_gacha setpool 1 |
| []     | 里面的值是可选的   | /arona_gacha reset [number]    | /arona_gacha reset     |
| a\|b   | 必须选择a或者b     |                                |                        |

#### 1. 抽卡配置

`/arona_gacha setpool <number>`  设置当前池子为数据库中主键为指定的`number`池子

`/arona_gacha reset [number]` 重置指定`number`池子的抽卡记录，`number`未指定时为当前池子

`/arona_gacha 1s <number>` 设置1星出货率为`number`

`/arona_gacha 2s <number>` 设置2星出货率`number`

`/arona_gacha 3s <number>` 设置3星出货率`number`

`/arona_gacha p2s <number>` 设置2星pick up出货率`number`

`/arona_gacha p3s <number>` 设置3星pick up出货率`number`

`/arona_gacha time <number>` 设置撤回时间为`number`秒

`/arona_gacha limit <number>` 设置每日限制次数`number`次

`/arona_gacha update <number>` 从远端更新id为`number`的池子

`/arona_gacha update2 <number> <string>` 从远端更新id为`number`的池子并重命名为`string`

`/arona_gacha list [number]` 查看最近`number`个池子的配置，`number`未指定时默认值为2

`/arona_gacha info <number>` 查看指定`number`池子的配置

**注意** 1、2、3星的出货率支持浮点数且它们的和需要等于100，2、3星pick up出货率不能高于各自的总出货率，否则抽卡功能可能不能正常运行

### webui
