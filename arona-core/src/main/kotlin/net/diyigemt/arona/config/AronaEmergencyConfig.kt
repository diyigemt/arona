package net.diyigemt.arona.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaEmergencyConfig : AutoSavePluginConfig("arona-emergency") {

  @ValueDescription("投票人数")
  val times: Int by value(5)

  @ValueDescription("统计的时间间隔,单位min")
  val duration: Int by value(5)

}