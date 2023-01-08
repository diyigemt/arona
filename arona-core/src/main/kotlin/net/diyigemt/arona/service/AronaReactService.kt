package net.diyigemt.arona.service

import net.mamoe.mirai.event.events.BotEvent
import kotlin.reflect.KClass

interface AronaReactService<T: BotEvent>: AronaService {

  val event: KClass<T>

  fun checkService(event: T): Boolean

  suspend fun handle(event: T)

}