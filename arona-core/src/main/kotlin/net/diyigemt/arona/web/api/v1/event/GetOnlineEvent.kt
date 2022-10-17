package net.diyigemt.arona.web.api.v1.event

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.message.MapMessage
import net.diyigemt.arona.web.api.v1.responseMessage

/**
 *@Author hjn
 *@Create 2022/10/17
 */
object GetOnlineEvent : Worker {
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    kotlin.runCatching {
      mapOf(Pair(AronaConfig.onlineMessage, AronaConfig.sendOnlineMessage))
    }.onSuccess {
      context.call.respond(responseMessage(MapMessage(it)))
    }.onFailure {
      context.call.respond(responseMessage(fail()))
    }
  }
}