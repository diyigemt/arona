package net.diyigemt.arona.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaServiceConfig: AutoSavePluginConfig("arona-service") {

  @ValueDescription("配置各功能模块的开关")
  val config: MutableMap<String, Boolean> by value()

}