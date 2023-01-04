package net.diyigemt.arona.web.api.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import net.diyigemt.arona.Arona
import net.diyigemt.arona.entity.ServerResponse
import net.diyigemt.arona.web.WebUIService

/**
 *@Author hjn
 *@Create 2022/11/14
 */
object PowerSupplyManager: Worker {
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    when(val option = context.call.parameters["option"]!!){
      "reboot" -> {
        val res = json.encodeToString(ServerResponse(HttpStatusCode.Gone.value, "Rebooting now", null as String?))
        context.call.respond(HttpStatusCode.Gone, res)
        Arona.runSuspend {
          Arona.info("WebUIService: Rebooting now")
          WebUIService.disableService()
          delay(1000)
          WebUIService.enableService()
        }
      }
      "shutdown" -> {
        val res = json.encodeToString(ServerResponse(HttpStatusCode.Gone.value, "Shutting down now", null as String?))
        context.call.respond(HttpStatusCode.Gone, res)
        WebUIService.disableService()
      }
      else -> {
        val res = json.encodeToString(ServerResponse(HttpStatusCode.BadRequest.value, "Undefined option: $option", null as String?))
        context.call.respond(HttpStatusCode.BadRequest, res)
      }
    }
  }
}