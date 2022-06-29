package net.diyigemt.arona.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaConfig: AutoSavePluginConfig("arona") {

  @ValueDescription("运行arona的qq")
  var qq: Long by value()

  @ValueDescription("arona服务的群")
  var groups: MutableList<Long> by value()

  @ValueDescription("发送arona上线消息")
  var sendOnlineMessage: Boolean by value(false)

  @ValueDescription("arona上线消息")
  var onlineMessage: String by value("arona醒了")

  @ValueDescription("发送arona下线消息")
  var sendOfflineMessage: Boolean by value(false)

  @ValueDescription("arona下线消息")
  var offlineMessage: String by value("arona摸鱼了")

}