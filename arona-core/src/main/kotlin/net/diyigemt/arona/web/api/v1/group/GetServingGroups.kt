package net.diyigemt.arona.web.api.v1.group

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.message.ListMessage
import net.diyigemt.arona.web.api.v1.responseMessage

/**
 *@Author hjn
 *@Create 2022/10/16
 */
object GetServingGroups : Worker{
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    kotlin.runCatching {
      AronaConfig.groups.toList()
    }.onSuccess {
      context.call.respond(responseMessage(ListMessage(it)))
    }.onFailure {
      context.call.respond(noContent())
    }
  }
}