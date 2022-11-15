package net.diyigemt.arona.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaWebUIConfig: AutoSavePluginConfig("arona-webui") {

  @ValueDescription("WebUI服务端口")
  val port: Int by value(8080)
}