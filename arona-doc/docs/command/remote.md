# 远端服务<a id="remote"> </a>

::: info

所有的远端服务均为作者手动维护，如果遇到信息更新延迟的情况说明作者懒狗了，请谅解。

:::

## 攻略指令

`/攻略`指令提供的所有地图、学生以及杂图图片全部保存在远端，在执行攻`/攻略`令时，arona将会查询本地文件夹下是否有对应的图片，若没有将会从远端进行下载。

同时，arona也会检查本地保存的图片与远端图片的md5校验值，若不一致将会重新下载。

以上两个过程，实现了攻略图片自动更新的功能。

## 公告<a id="remote-announce"> </a>

arona将会根据`arona.yml`配置文件中的`remoteCheckInterval`配置项定期向远端查询新的公告信息，下面是一个示例：

::: details 公告示例

<img src="/image/command/announcment.webp" alt="announcment" />

:::

基本上是用来通知攻略图片的更新信息的。

## 卡池自动更新<a id="remote-pool-update"> </a>

arona将会根据`arona.yml`配置文件中的`remoteCheckInterval`配置项定期向远端查询新的卡池，下面是一个示例：

::: details 卡池更新示例

<img src="/image/command/pool-update.webp" alt="pool-update" />

:::

设置成手动更新的原因是怕用户自己已经有这个池子了。
