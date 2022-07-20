package net.diyigemt.arona.handler

import net.diyigemt.arona.config.AronaHentaiConfig
import net.diyigemt.arona.service.AronaGroupService
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.event.events.GroupMessageEvent

object HentaiEventHandler: AronaEventHandler<GroupMessageEvent>, AronaGroupService {

  override suspend fun handle(event: GroupMessageEvent) {
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

  override val id: Int = 9
  override val name: String = "发情"
  override var enable: Boolean = true

  override fun init() {
    registerService()
  }
}