# BA bot攻略数据公开计划

该api最初是作为[arona bot](https://github.com/diyigemt/arona)的服务而开发出来的，现在由于tx疯狂打压mirai系bot的生存空间，看不到活着的希望，于是决定公开，希望能帮助到其他类型的bot

具体效果可以看站里的[功能介绍](/command/manual#%E5%AD%A6%E7%94%9F%E4%B8%8E%E4%B8%BB%E7%BA%BF%E5%9C%B0%E5%9B%BE%E6%94%BB%E7%95%A5%E7%B3%BB%E5%88%97)

## 前言

1. 所有图片均来源于网络，在非wiki处收集来的图片已经打上出处，希望各位使用时不要将出处抹去
2. 所有图片均为人工整理，因此出现错漏可以联系我
   - [gmail](qwe1355247243@gmail.com)
   - [github](https://github.com/diyigemt/arona)
   - [bilibili](https://space.bilibili.com/6690298)
   - [nga](https://nga.178.com/nuke.php?func=ucp&uid=42164110)
3. 使用本 API 是你个人的行为，产生的任何问题都与我无关
4. 在使用过程中如果出现图片无法发出的情况，可以去[腾讯文档的表格](https://docs.qq.com/sheet/DVGNTT3hCVUJKVHZP?tab=BB08J2)反馈

## 攻略系列

### 主线·学生·活动等攻略

实现效果可看[功能介绍](/command/manual#%E5%AD%A6%E7%94%9F%E4%B8%8E%E4%B8%BB%E7%BA%BF%E5%9C%B0%E5%9B%BE%E6%94%BB%E7%95%A5%E7%B3%BB%E5%88%97)

```shell
GET https://arona.diyigemt.com/api/v1/image
```

请求参数

| 参数名  | 数据类型   | 是否必须 | 说明       |
|------|--------|------|----------|
| name | string | 是    | 要查找的攻略名称 |

返回值

| 参数名     | 数据类型         | 说明                                                         |
|---------|--------------|------------------------------------------------------------|
| status  | int          | 101: 请求错误(没name)/模糊搜索结果, 200: 请求成功                         |
| data    | ResultList[] | 结果集                                                        |
| message | string       | name is empty: 没name, fuse search: 模糊搜索结果, wrong name: 真没有 |

ResultList

| 参数名  | 数据类型   | 说明                            |
|------|--------|-------------------------------|
| id   | int    | id                            |
| name | string | 查询名称                          |
| path | string | 图片路径/拼音                       |
| hash | string | 图片md5结果                       |
| type | int    | 图片类型, 1: 学生攻略, 2: 主线地图, 3: 杂图 |

当`name`没有精确对应结果时, 接口会返回模糊查询结果, 此时`data`有两种可能, list或者null
- 为null表明连模糊查询结果都没有
- 为list时, list的length只可能为4

可以通过data存在与否以及list的len判断结果类型

当为精确匹配时, data为list且其len必为1, 此时可通过item的`hash`判断是否有更新, 并通过`path`获取文件地址

精确匹配查询示例

```bash
GET https://arona.diyigemt.com/api/v1/image?name=国际服未来视
```

精确匹配返回示例

```json
{
  "status": 200,
  "data": [
    {
      "name": "国际服未来视",
      "path": "/some/国际服未来视.png",
      "hash": "46f118f546424b96193c117dc2dc0470",
      "type": 3
    }
  ],
  "message": ""
}
```

此时文件地址为 `https://arona.cdn.diyigemt.com/image/some/国际服未来视.png`

模糊查询结果示例

```bash
GET https://arona.diyigemt.com/api/v1/image?name=国际服
```

模糊查询返回示例

```json
{
  "status": 101,
  "data": [
    {
      "id": 493,
      "name": "国际服竞技场",
      "path": "guojifujingjichang",
      "hash": "2fce6f88fbc106751c02a0224ea77edc",
      "type": 0
    },
    {
      "id": 559,
      "name": "国际服室内鸡",
      "path": "guojifushineiji",
      "hash": "ecd2298b56753f11ad09ba966823d3b9",
      "type": 0
    },
    {
      "id": 713,
      "name": "国际服室外鸡",
      "path": "guojifushiwaiji",
      "hash": "5562ee2b52ac57d5ff2c5c2f7d681ffe",
      "type": 0
    },
    {
      "id": 734,
      "name": "国际服室内GOZ",
      "path": "guojifushineiGOZ",
      "hash": "a7c7e8c5c983aef47120483811599fc0",
      "type": 0
    }
  ],
  "message": "fuse search"
}
```

此时可通过`item`字段让用户选择具体的内容后再次通过接口获取精确结果

::: danger

如你所见, 文件资源是部署在cdn上的, 所以希望你能读完下面的话

:::

### cdn的使用

为了加快响应速度, 所有文件资源均部署在国内cdn服务上, 前缀为`https://arona.cdn.diyigemt.com/image`

由于cdn不是白嫖的, **希望**各位开发者善用`hash`字段

理想的情况是, 获取请求结果->检查hash是否变更, 如果变更则下载新资源, 否则继续使用旧资源

`hash`采用**md5**进行计算, 可以使用数据库缓存上次请求的结果, 也可以每次都计算一次

### 默认情况

| name    | 说明              |
|---------|-----------------|
| 国际服未来视  | 猫佬的未来视          |
| 国际服总力   | 始终指向国际服当期总力     |
| 国际服火力演习 | 始终指向国际服当期火力演习   |
| 国际服活动   | 始终指向国际服当期活动     |
| 日服总力    | 始终指向日服当期总力      |
| 日服火力演习  | 始终指向日服当期火力演习    |
| 日服活动    | 始终指向日服当期活动      |
| 杂图      | 指向所有不好记的名称的图片汇总 |

::: warning

再次强调, 所有图片都是人工整理, 难免会有错漏和更新不及时的情况, 请见谅

:::

## action系列

机器人特供的, 可以不用

```bash
GET https://arona.diyigemt.com/api/v1/action?read=
```

请求参数

| 参数名  | 数据类型   | 是否必须 | 说明    |
|------|--------|------|-------|
| read | string | 是    | 已读的id |

为列表`join`的形式, 最长为10个, 如:`read=1,2,3`

当`read`为空字串时, 返回最近三天内的记录

返回值


| 参数名     | 数据类型         | 说明                          |
|---------|--------------|-----------------------------|
| status  | int          | 101: 请求错误(没read), 200: 请求成功 |
| data    | ResultList[] | 结果集                         |
| message | string       |                             |

ResultList

| 参数名     | 数据类型   | 说明                       |
|---------|--------|--------------------------|
| id      | int    | id                       |
| action  | string | 类型                       |
| content | string | json序列化结果                |
| time    | string | 发布时间 yyyy-MM-dd HH:mm:ss |

### announcement

当`action`为`announcement`时, 该动作为一条公告, 此时content为纯字符串

### pool update

当`action`为`poolUpdate`时, 该动作为卡池更新, 此时content为ResultList的json序列化结果

| 参数名       | 数据类型   | 说明   |
|-----------|--------|------|
| name      | string | 卡池名称 |
| character | Item[] | 卡池内容 |

Item

| 参数名   | 数据类型   | 说明             |
|-------|--------|----------------|
| name  | string | 角色名称           |
| star  | int    | 星级 1,2,3       |
| limit | int    | 是否限定 0: 否 1: 是 |

请求示例

```bash
GET https://arona.diyigemt.com/api/v1/action?read=
```

返回示例

```json
{
  "status": 200,
  "data": [
    {
      "id": 111,
      "action": "announcement",
      "content": "过年了过年了https://kivo.wiki/",
      "time": "2023-06-21 12:14:46"
    },
    {
      "id": 112,
      "action": "poolUpdate",
      "content": "{\"name\":\"泳装不知道第几期池\",\"character\":[{\"name\":\"宫子(泳装)\",\"star\":3,\"limit\":0},{\"name\":\"咲(泳装)\",\"star\":3,\"limit\":0}]}",
      "time": "2023-06-21 14:38:19"
    },
    {
      "id": 113,
      "action": "announcement",
      "content": "新攻略内容：国服角色简评",
      "time": "2023-06-21 18:10:34"
    },
    {
      "id": 114,
      "action": "announcement",
      "content": "把猫佬最新的评分更新了,有错的图片吱一声",
      "time": "2023-06-22 22:34:56"
    }
  ],
  "message": ""
}
```