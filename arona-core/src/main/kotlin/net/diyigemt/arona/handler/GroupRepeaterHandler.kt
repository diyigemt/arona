package net.diyigemt.arona.handler

import net.diyigemt.arona.config.AronaRepeatConfig
import net.diyigemt.arona.service.AronaGroupService
import net.diyigemt.arona.service.AronaMessageReactService
import net.mamoe.mirai.event.events.GroupMessageEvent

// 复读
object GroupRepeaterHandler: AronaMessageReactService<GroupMessageEvent>, AronaGroupService {
  private var last: String = ""
  private var lastSender: Long = 0
  private var count: Int = 0
  override suspend fun handle(event: GroupMessageEvent) {
    val now = event.message.serializeToMiraiCode()
    if (now.startsWith("/")) return
    val senderId = event.sender.id
    if (now == last && senderId != lastSender) {
      count++
      if (count >= AronaRepeatConfig.times) {
        event.subject.sendMessage(event.message)
        count = 0
        last = now
        lastSender = 0
      }
    } else {
      last = now
      lastSender = senderId
      count = 1
    }
  }

  override val eventName: String? = GroupMessageEvent::class.simpleName

  override val id: Int = 8
  override val name: String = "复读"
  override var enable: Boolean = true
  override val description: String = name
}