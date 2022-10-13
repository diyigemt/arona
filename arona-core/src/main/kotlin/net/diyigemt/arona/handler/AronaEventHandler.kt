package net.diyigemt.arona.handler

import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.service.AronaServiceManager
import net.mamoe.mirai.event.events.MessageEvent

interface AronaEventHandler<in T: MessageEvent>: AronaService {

  suspend fun preHandle(event: T) {
    if (AronaServiceManager.checkService(this, event.sender, event.subject) == null) {
      handle(event)
    }
  }
  suspend fun handle(event: T)
}