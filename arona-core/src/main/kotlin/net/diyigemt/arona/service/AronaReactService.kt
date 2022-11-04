package net.diyigemt.arona.service

import net.mamoe.mirai.event.events.BotEvent

interface AronaReactService<in T: BotEvent>: AronaService {

  val eventName: String?

  fun checkService(event: T): Boolean

  suspend fun handle(event: T)

}