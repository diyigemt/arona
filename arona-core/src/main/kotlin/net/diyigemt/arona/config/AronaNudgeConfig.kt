package net.diyigemt.arona.config

import net.diyigemt.arona.entity.ReplyMessage
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaNudgeConfig: AutoSavePluginConfig("arona-nudge") {

  @ValueDescription("回复的消息")
  var messageList: MutableList<ReplyMessage> by value()

}