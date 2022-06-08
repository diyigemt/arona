package net.diyigemt.arona.handler

import net.mamoe.mirai.event.events.GroupMessageEvent

// 复读
object GroupRepeaterHandler: AronaEventHandler<GroupMessageEvent> {
  private var last: String = ""
  private var count: Int = 0
  override suspend fun handle(event: GroupMessageEvent) {
    val now = event.message.contentToString()
    if (now == last) {
      count++
      if (count > 3) {
        event.subject.sendMessage(event.message)
        count = 0
      }
    } else {
      last = now
      count = 0
    }
  }
}