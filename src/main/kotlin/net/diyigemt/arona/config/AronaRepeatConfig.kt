package net.diyigemt.arona.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaRepeatConfig: AutoSavePluginConfig("arona-repeat") {

  @ValueDescription("是否开启复读功能")
  var enable: Boolean by value(false)

  @ValueDescription("复读次数")
  var times: Int by value(3)

}