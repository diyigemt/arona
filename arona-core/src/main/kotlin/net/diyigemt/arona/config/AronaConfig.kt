package net.diyigemt.arona.config

import net.diyigemt.arona.annotations.Const
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaConfig : AutoSavePluginConfig("arona") {

  @ValueDescription("运行arona的qq")
  var qq: Long by value()

  @ValueDescription("arona服务的群")
  var groups: MutableList<Long> by value()

  @ValueDescription("具有管理员权限的qq号")
  var managerGroup: MutableList<Long> by value()

  @ValueDescription("当不具有管理员的用户尝试执行需要管理权限的指令时的回复消息,为空则不回复")
  var permissionDeniedMessage: String by value()

  @ValueDescription("发送arona上线消息")
  var sendOnlineMessage: Boolean by value(false)

  @ValueDescription("arona上线消息")
  var onlineMessage: String by value("arona活了")

  @ValueDescription("发送arona下线消息")
  var sendOfflineMessage: Boolean by value(false)

  @ValueDescription("arona下线消息")
  var offlineMessage: String by value("arona摸了")

  @ValueDescription("自动检查更新的时间")
  var updateCheckTime: Int by value(8)

  @ValueDescription("名称是否自动带上后缀, 默认是\"老师\"")
  var endWithSensei: String by value("老师")

  @ValueDescription("是否自动配置权限")
  var autoGrantPermission: Boolean by value(true)

  @ValueDescription("是否允许arona收集匿名统计信息")
  var sendStatus: Boolean by value(false)

  @Const
  @ValueDescription("识别id(无需修改)")
  var uuid: String by value("")

  @ValueDescription("远端操作查询间隔 设置为0表示不开启, 单位是小时")
  var remoteCheckInterval: Int by value(1)

}