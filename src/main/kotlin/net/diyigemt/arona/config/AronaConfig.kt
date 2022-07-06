package net.diyigemt.arona.config

import net.diyigemt.arona.entity.ActivityType
import net.diyigemt.arona.entity.ServerLocale
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaConfig: AutoSavePluginConfig("arona") {

  @ValueDescription("运行arona的qq")
  val qq: Long by value()

  @ValueDescription("arona服务的群")
  val groups: MutableList<Long> by value()

  @ValueDescription("发送arona上线消息")
  val sendOnlineMessage: Boolean by value(false)

  @ValueDescription("arona上线消息")
  val onlineMessage: String by value("arona活了")

  @ValueDescription("发送arona下线消息")
  val sendOfflineMessage: Boolean by value(false)

  @ValueDescription("arona下线消息")
  val offlineMessage: String by value("arona摸了")

  @ValueDescription("\"/活动\"指令的默认目标服务器,可选值为 \"JP\"和\"GLOBAL\"")
  val defaultActivityCommandServer: ServerLocale by value(ServerLocale.JP)

}