package net.diyigemt.arona.handler

import net.diyigemt.arona.config.AronaRepeatConfig
import net.diyigemt.arona.service.AronaGroupService
import net.mamoe.mirai.event.events.GroupMessageEvent

// 复读
object GroupRepeaterHandler: AronaEventHandler<GroupMessageEvent>, AronaGroupService {
  private val map = mutableMapOf<Long, Triple<String, Long, Int>>() // 群 -> 上次消息, 上次发送者, 次数
  override suspend fun handle(event: GroupMessageEvent) {
    val now = event.message.serializeToMiraiCode()
    if (now.startsWith("/")) return
    val senderId = event.sender.id
    val group = event.group.id
    val last = map[group]
    if (last == null) {
      map[group] = Triple(now, senderId, 1)
      return
    }
    if (now == last.first && senderId != last.second) {
      val count = last.third + 1
      if (count >= AronaRepeatConfig.times) {
        event.subject.sendMessage(event.message)
        map[group] = Triple(now, 0, 0)
      }
    } else {
      map[group] = Triple(now, senderId, 1)
    }
  }

  override val id: Int = 8
  override val name: String = "复读"
  override var enable: Boolean = true

  override fun init() {
    registerService()
  }
}
