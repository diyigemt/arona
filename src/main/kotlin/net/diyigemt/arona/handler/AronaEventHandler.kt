package net.diyigemt.arona.handler

import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.service.AronaServiceManager
import net.diyigemt.arona.util.GeneralUtils
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.NudgeEvent

interface AronaEventHandler<in T: MessageEvent>: AronaService {

  suspend fun preHandle(event: T) {
    if (AronaServiceManager.checkService(this, event.sender, event.subject) == null) {
      handle(event)
    }
  }
  suspend fun handle(event: T)
}