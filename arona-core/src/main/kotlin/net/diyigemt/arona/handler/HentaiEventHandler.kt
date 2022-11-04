package net.diyigemt.arona.handler

import net.diyigemt.arona.Arona.sendTeacherNameMessage
import net.diyigemt.arona.config.AronaHentaiConfig
import net.diyigemt.arona.service.AronaGroupService
import net.diyigemt.arona.service.AronaMessageReactService
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.contact.UserOrBot
import net.mamoe.mirai.event.events.GroupMessageEvent

object HentaiEventHandler: AronaMessageReactService<GroupMessageEvent>, AronaGroupService {

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
        subject.sendTeacherNameMessage(target as UserOrBot, MessageUtil.at(target, msg.message))
        return
      } else {
        index -= msg.weight
      }
    }
  }

  override val eventName: String? = GroupMessageEvent::class.simpleName
  override val id: Int = 9
  override val name: String = "发情"
  override var enable: Boolean = true
  override val description: String = name
}