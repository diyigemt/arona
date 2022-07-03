package net.diyigemt.arona.handler

import net.diyigemt.arona.util.GeneralUtils
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NudgeEvent

interface AronaEventHandler<in T: Event> {

  suspend fun preHandle(event: T) {
    val flag = when(event) {
      is GroupMessageEvent -> GeneralUtils.checkService(event.subject)
      is NudgeEvent -> GeneralUtils.checkService(event.subject)
      else -> true
    }
    if (flag) handle(event)
  }
  suspend fun handle(event: T)
}