package net.diyigemt.arona.config

import net.diyigemt.arona.entity.ReplyRule
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaHentaiConfig: AutoSavePluginConfig("arona-hentai") {

  @ValueDescription("条件")
  var rules: MutableList<ReplyRule> by value()

}