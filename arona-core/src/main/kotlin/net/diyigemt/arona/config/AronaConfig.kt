package net.diyigemt.arona.config

import net.diyigemt.arona.advance.NGAImageTranslatePusher
import net.diyigemt.arona.annotations.ConfigKey
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaConfig : AutoSavePluginConfig("arona") {

  @ValueDescription("运行arona的qq")
  var bots: MutableList<Long> by value()

  @ValueDescription("具有管理员权限的qq号")
  var managerGroup: MutableList<Long> by value()

  @ValueDescription("webui监听端口")
  @ConfigKey("webui.port")
  val webuiPort: Int by value(8080)

}