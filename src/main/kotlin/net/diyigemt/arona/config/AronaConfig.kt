package net.diyigemt.arona.config

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

  @ValueDescription("是否允许arona收集匿名统计信息")
  val sendStatus: Boolean by value(false)

}