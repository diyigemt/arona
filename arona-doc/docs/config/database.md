# 数据库

**注意**，可能会夹带私货。

数据库目前有四张表，主要是为了抽卡模块服务。

## GachaCharacters表

用以保存池子中的所有学生信息并区分是否是限定。

| 键     | 属性          | 作用      |
|-------|-------------|---------|
| id    | INTEGER     | 主键      |
| name  | VARCHAR(10) | 学生名字    |
| star  | INT         | 学生初始星级  |
| limit | BOOL        | 学生是否是限定 |

## GachaPools表

用以保存各池子的信息。

| 键    | 属性          | 作用   |
|------|-------------|------|
| id   | INTEGER     | 主键   |
| name | VARCHAR(50) | 池子名字 |

## GachaPoolCharacters表

用以保存各池子中限定pickup的信息。

| 键            | 属性      | 作用          |
|--------------|---------|-------------|
| id           | INTEGER | 主键          |
| pool_id      | INTEGER | 外键，指向限定池表主键 |
| character_id | INTEGER | 外键，指向学生表主键  |

## GachaHistoryTable表

用以保存抽卡记录。

| 键      | 属性      | 作用                        |
|--------|---------|---------------------------|
| qq     | BIGINT  | 主键1，是群员QQ号                |
| group  | BIGINT  | 主键2，群员所在群号                |
| pool   | INTEGER | 主键3，外键，指向限定池表主键，区分不同池子的记录 |
| points | INTEGER | 记录这个池子抽了多少发               |
| count3 | INTEGER | 记录抽出几个三星                  |
| dog    | INTEGER | 记录几抽抽出pickup              |

目前数据库中记录了5个池子，它们的id的对应内容为：

| id | 内容      |
|----|---------|
| 1  | 普池      |
| 2  | 初音池     |
| 3  | 公主池     |
| 4  | nnm大狐狸池 |
| 5  | 亚子池     |
| 6  | 小夏池     |
| 7  | 水大叔池    |
| 8  | 水千世老板娘池 |
| 9  | 爱丽丝骑白子池 |
| 10 | 86新春池   |
| 11 | 纱织池     |
| 12 | 春黑池     |
| 13 | 小黑猫大兔子  |
| 14 | 大狐狸     |
| 15 | 泳装2期    |

## GachaLimit表

保存抽卡限制信息

| 键     | 属性      | 作用          |
|-------|---------|-------------|
| qq    | BIGINT  | 主键1，也是群员QQ号 |
| group | BIGINT  | 主键2，群员所在群号  |
| count | INTEGER | 今天抽了几次      |

## Tarot表

保存塔罗牌信息

| 键        | 属性       | 作用   |
|----------|----------|------|
| id       | INTEGER  | 主键   |
| name     | CHAR(30) | 牌名   |
| positive | TEXT     | 正位解释 |
| negative | TEXT     | 逆位解释 |

## TarotRecord表

保存塔罗牌抽取信息信息，当`arona-tarot.yml->dayOne=false`时不使用

| 键        | 属性      | 作用              |
|----------|---------|-----------------|
| qq       | BIGINT  | 主键1，也是群员QQ号     |
| group    | BIGINT  | 主键2，群员所在群号      |
| tarot    | INTEGER | 外键，对应Tarot表中的id |
| positive | BOOL    | true:正位         |

## TeacherName表

保存群友自定义的昵称

| 键     | 属性     | 作用          |
|-------|--------|-------------|
| qq    | BIGINT | 主键1，也是群员QQ号 |
| group | BIGINT | 主键2，群员所在群号  |
| name  | CHAR   | 昵称          |

## Image表

保存本地图片文件信息

| 键    | 属性      | 作用                                      |
|------|---------|-----------------------------------------|
| id   | BIGINT  | 主键                                      |
| name | CHAR    | `/攻略`指令对应的名称                            |
| path | CHAR    | 文件相对于./data/net.diyigemt.arona/image的路径 |
| hash | CHAR    | 文件md5值，据此判断文件是否需要更新                     |
| type | INTEGER | 文件类型，留着扩展用                              |

## 新卡池添加方法<a id="setpool-config" />

1. 停止mirai-console的运行；
2. 从`./data/net.diyigemt.arona/arona.db`获取db文件；
3. 编辑db文件，在GachaCharacters表中插入新学生的信息；
4. 在GachaPools表中插入新池的信息；
5. 在GachaPoolCharacters表中插入新池与限定角色的关联信息；
6. 使用编辑好的数据库文件替换`./data/net.diyigemt.arona/arona.db`下的文件；
7. 启动mirai-console，在群聊中发送指令`/gacha setpool <number>`，其中number为第4步中新池的主键

如果你不会使用SQLite也没关系，下面我模拟一下这种情况，比如日服新出了粉狐狸然后up了，该如何添加这个池子。

1. 下载任意一个可以编辑SQLite的软件，比如[sqlite-gui](https://github.com/little-brother/sqlite-gui/releases/download/1.7.5/sqlite-gui.1.7.5-x64.zip)
2. 使用工具栏中的`open`选项打开获取到的池子文件，如下图

::: details 步骤2

<img src="/image/config/db-2.JPG" alt="" />

:::

3. 双击`Tables`栏中的`GachaCharacters`数据表，编辑新学生信息

::: details 步骤3

<img src="/image/config/db-3.JPG" alt="" />

:::

其中`name`为学生名字;`star`为学生初始星级；`limit`表示学生是限定还是常驻，其中1表示限定、0表示常驻；`id`为自动生成，无需填写。

填写完毕后单击`Save and New`保存，即可关闭页面。

4. 双击`Tables`栏中的`GachaPools`数据表，编辑新池子信息

::: details 步骤4

<img src="/image/config/db-4.JPG" alt="" />

:::

同上，只需要填入`name`作为新池子的名字即可。

5. 双击`Tables`栏中的`GachaPoolCharacters`数据表，编辑新池子与新学生的对应关系信息

::: details 步骤5-1

<img src="/image/config/db-5-1.JPG" alt="" />

:::

其中`pool_id`为之前新建池子的主键，可以在这里看到

::: details 步骤5-2

<img src="/image/config/db-5-2.JPG" alt="" />

:::

同理，`character_id`为之前新建的学生信息的主键

::: details 步骤5-3

<img src="/image/config/db-5-3.JPG" alt="" />

:::

**特别的**，一个池子可以有多个pickup，因此可以根据需要在`GachaPoolCharacters`对同一个池子添加多条数据。

至此，数据库编辑完毕。

6. 使用编辑好的数据库文件替换`./data/net.diyigemt.arona/arona.db`文件；
7. 启动mirai-cosole，在群聊中发送指令`/gacha setpool <number>`，其中number为第4步中新池的主键，即可启用新的池子。
