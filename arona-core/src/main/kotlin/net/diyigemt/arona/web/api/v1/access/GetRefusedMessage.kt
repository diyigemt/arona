package net.diyigemt.arona.web.api.v1.access

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.message.SingleMessage
import net.diyigemt.arona.web.api.v1.responseMessage

/**
 *@Author hjn
 *@Create 2022/10/17
 */
object GetRefusedMessage : Worker {
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    kotlin.runCatching {
      AronaConfig.permissionDeniedMessage
    }.onSuccess {
      context.call.respond(responseMessage(SingleMessage(it)))
    }.onFailure {
      context.call.respond(fail())
    }
  }
}