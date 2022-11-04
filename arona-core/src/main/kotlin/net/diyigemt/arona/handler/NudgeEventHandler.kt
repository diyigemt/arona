package net.diyigemt.arona.handler

import net.diyigemt.arona.Arona.sendTeacherNameMessage
import net.diyigemt.arona.config.AronaNudgeConfig
import net.diyigemt.arona.service.*
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.NudgeEvent

object NudgeEventHandler: AronaReactService<NudgeEvent>, AronaGroupService {

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
        subject.sendTeacherNameMessage(from, MessageUtil.atAndCTRL(from as User, msg.message))
        return
      } else {
        index -= msg.weight
      }
    }
  }

  override val eventName: String? = NudgeEvent::class.simpleName
  override fun checkService(event: NudgeEvent): Boolean = AronaServiceManager.checkService(this, event.from as User, event.subject) == null
  override val id: Int = 10
  override val name: String = "摸头回复"
  override val description: String = name
  override var enable: Boolean = true

}