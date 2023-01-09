package net.diyigemt.arona.service

import net.mamoe.mirai.event.events.MessageEvent

interface AronaMessageReactService<T: MessageEvent>: AronaReactService<T> {

  override fun checkService(event: T): Boolean {
    return true
  }

}