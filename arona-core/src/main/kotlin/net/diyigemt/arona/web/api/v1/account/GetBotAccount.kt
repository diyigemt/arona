package net.diyigemt.arona.web.api.v1.account

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import net.diyigemt.arona.Arona
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.message.SingleMessage
import net.diyigemt.arona.web.api.v1.responseMessage

/**
 *@Author hjn
 *@Create 2022/10/16
 */
object GetBotAccount : Worker {
    override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
      kotlin.runCatching {
        Arona.arona.id
      }.onSuccess {
        context.call.respond(responseMessage(SingleMessage(it)))
      }.onFailure {
        context.call.respond(noContent())
      }
    }
}