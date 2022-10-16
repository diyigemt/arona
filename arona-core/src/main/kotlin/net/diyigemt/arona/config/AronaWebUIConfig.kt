package net.diyigemt.arona.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaWebUIConfig: AutoSavePluginConfig("arona-webui") {

  @ValueDescription("webui的认证密码")
  val auth: String by value()

}