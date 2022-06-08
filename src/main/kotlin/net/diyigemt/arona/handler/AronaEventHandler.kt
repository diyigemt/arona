package net.diyigemt.arona.handler

import net.mamoe.mirai.event.Event

interface AronaEventHandler<in T: Event> {
  suspend fun handle(event: T)
}