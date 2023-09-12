package net.diyigemt.arona.config

import net.diyigemt.arona.extension.TempMessageIgnoreType
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaConfig: AutoSavePluginConfig("arona") {

  @ValueDescription("运行arona的qq")
  val qq: Long by value()

  @ValueDescription("arona服务的群")
  val groups: MutableList<Long> by value()

  @ValueDescription("具有管理员权限的qq号")
  val managerGroup: MutableList<Long> by value()

  @ValueDescription("当不具有管理员的用户尝试执行需要管理权限的指令时的回复消息,为空则不回复")
  val permissionDeniedMessage: String by value()

  @ValueDescription("发送arona上线消息")
  val sendOnlineMessage: Boolean by value(false)

  @ValueDescription("arona上线消息")
  val onlineMessage: String by value("arona活了")

  @ValueDescription("发送arona下线消息")
  val sendOfflineMessage: Boolean by value(false)

  @ValueDescription("arona下线消息")
  val offlineMessage: String by value("arona摸了")

  @ValueDescription("自动检查更新的时间")
  val updateCheckTime: Int by value(8)

  @ValueDescription("名称是否自动带上后缀, 默认是\"老师\"")
  val endWithSensei: String by value("老师")

  @ValueDescription("是否允许arona收集匿名统计信息")
  val sendStatus: Boolean by value(false)

  @ValueDescription("识别id(无需修改)")
  var uuid: String by value("")

  @ValueDescription("远端操作查询间隔 设置为0表示不开启, 单位是小时")
  val remoteCheckInterval: Int by value(1)

  @ValueDescription("是否忽略私聊指令, NONE: \"不忽略\";ONLY_SERVICE_GROUP: \"忽略不在groups群里的群友\";ALL: \"忽略所有\"")
  val tempMessageIgnoreType: TempMessageIgnoreType by value(TempMessageIgnoreType.NONE)

  @ValueDescription("内置流量网络代理, 比如活动获取/和后端通信, 海外用户可能需要")
  val proxyHost: String by value()

  @ValueDescription("内置流量网络代理, 比如活动获取/和后端通信, 海外用户可能需要")
  val proxyPort: Int by value()

}
