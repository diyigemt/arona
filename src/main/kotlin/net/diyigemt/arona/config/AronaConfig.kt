package net.diyigemt.arona.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaConfig: AutoSavePluginConfig("arona") {

  @ValueDescription("用户arona的qq")
  var qq: Long by value()

  @ValueDescription("用户arona的群")
  var groups: MutableList<Long> by value()

}