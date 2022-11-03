package net.diyigemt.arona.service

import net.mamoe.mirai.event.events.BotEvent

interface AronaReactService<in T: BotEvent>: AronaService {

  fun checkService(event: T): Boolean

  suspend fun handle(event: T)

}