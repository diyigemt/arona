package net.diyigemt.arona.handler

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaHentaiConfig
import net.diyigemt.arona.config.AronaNudgeConfig
import net.diyigemt.arona.entity.NudgeMessage
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NudgeEvent

object HentaiEventHandler: AronaEventHandler<GroupMessageEvent> {

  override suspend fun handle(event: GroupMessageEvent) {
    val enable = AronaHentaiConfig.enable
    if (!enable) return
    val source = event.message.contentToString()
    if (!(source.contains("老婆") || source.contains("老公"))) return
    val messageList = AronaHentaiConfig.messageList
    val listen = AronaHentaiConfig.listen
    val target = event.sender
    val subject = event.subject
    if (!listen.contains(target.id)) return
    val total = messageList.sumOf { it.weight }
    var index = (0 until total).random()
    for (msg in messageList) {
      if (index < msg.weight) {
        subject.sendMessage(MessageUtil.at(target, msg.message))
        return
      } else {
        index -= msg.weight
      }
    }
  }

}