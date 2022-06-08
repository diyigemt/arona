package net.diyigemt.arona.handler

import net.diyigemt.arona.config.AronaNudgeConfig
import net.diyigemt.arona.entity.NudgeMessage
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.NudgeEvent

object NudgeEventHandler: AronaEventHandler<NudgeEvent> {

  override suspend fun handle(event: NudgeEvent) {
    val messageList = AronaNudgeConfig.messageList
    val target = event.target
    val from = event.from
    val subject = event.subject
    val bot = event.bot
    if (target.id != bot.id || target.id == from.id || messageList.isEmpty()) return
    if (subject !is Group) return
    val total = messageList.sumOf { it.weight }
    var index = (0 until total).random()
    for (msg in messageList) {
      if (index < msg.weight) {
        subject.sendMessage(MessageUtil.atAndCTRL(from as User, msg.message))
      } else {
        index -= msg.weight
      }
    }
  }

}