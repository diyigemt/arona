# Arona-WebUI-Backend-API

Version 1.0



## API Prefix

```http
/api/v1/
```



## Response

1. 所有的API的回复Header中都拥有Content-type: application/json
2. 所有的回复都拥有以下特性，以下不再说明这些信息只说明data的JSON结构和具体可能返回的HTML状态值：

外层结构：

| varName | Type   | Description |
| ------- | ------ | ----------- |
| code    | Number | HTML状态码  |
| message | String | 状态描述    |
| data    | JSON   | 请求的数据  |



可能的状态码：

| varName | Value   | Description                  |
| ------- | ------- | ---------------------------- |
| code    | 200/500 | 成功返回200，内部错误返回500 |



## Config

获取配置信息

### AronaConfig

```http
config/aronaConfig
```



可接受的请求方法：GET



data成员变量：

| varName                 | Type        |
| ----------------------- | ----------- |
| endWithSensei           | ContentUnit |
| group                   | ContentUnit |
| managerGroup            | ContentUnit |
| offlineMessage          | ContentUnit |
| onlineMessage           | ContentUnit |
| permissionDeniedMessage | ContentUnit |
| qq                      | ContentUnit |
| remoteCheckInterval     | ContentUnit |
| sendOfflineMessage      | ContentUnit |
| sendOnlineMessage       | ContentUnit |
| sendStatus              | ContentUnit |
| updateCheckTime         | ContentUnit |
| uuid                    | ContentUnit |



  ContentUnit类型组成：

| varName     | Type   | Description                   |
| ----------- | ------ | ----------------------------- |
| value       | Any    | 上表中任意ContentUnit类型的值 |
| description | String | 上表中任意成员在YAML中的描述  |



### Commit

提交所有Config的修改

```http
config/commit
```



可接受的请求方法：POST

可接受的数据：JSON



JSON格式：

```json
{
    "ConfigName.propertyName1" : value,
    "ConfigName.propertyName2" : value,
    ...
}
```

注：value的类型随意，反序列化后能对上Config类中相应成员类型即可。



data成员变量：

| varName                 | Type    | Description                                     |
| ----------------------- | ------- | ----------------------------------------------- |
| ConfigName.propertyName | Boolean | POST提交的设置。修改成功为true，修改失败为false |

e.g:

```json
{
    "ConfigName.propertyName1" : true,
    "ConfigName.propertyName2" : false,
    ...
}
```



## Contacts

获取Bot所登录账号的群和好友信息

```http
contacts
```



可接受的请求方法：GET



data成员变量：

| varName | Type | Description |
| ------- | ---- | ----------- |
| groups  | List | 群列表      |
| friends | List | 好友列表    |



group节点结构：

| varName | Type   | Description |
| ------- | ------ | ----------- |
| id      | Number | 群号        |
| name    | String | 群名        |



group节点结构：

| varName | Type   | Description                        |
| ------- | ------ | ---------------------------------- |
| id      | Number | 好友QQ号                           |
| name    | String | 好友昵称                           |
| remark  | String | 好友昵称备注（无备注时与昵称一致） |

