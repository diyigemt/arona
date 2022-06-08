package net.diyigemt.arona.handler

import net.mamoe.mirai.event.events.GroupMessageEvent
import java.awt.image.BufferedImage

// 复读
object GroupRepeaterHandler: AronaEventHandler<GroupMessageEvent> {
  private var last: String = ""
  private var count: Int = 0
  override suspend fun handle(event: GroupMessageEvent) {
    val now = event.message.contentToString()
    if (now == last) {
      count++
      if (count > 2) {
        event.subject.sendMessage(event.message)
        count = 0
        last = now
      }
    } else {
      last = now
      count = 0
    }
  }
}