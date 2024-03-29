package net.diyigemt.arona.web.api.v1.blockly

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.message.ServerResponse
import net.diyigemt.arona.web.blockly.BlocklyService
import net.diyigemt.arona.web.blockly.SaveManager

/**
 *@Author hjn
 *@Create 2022/12/26
 */
object BlocklyAPIService: Worker {
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    if (context.call.request.httpMethod == HttpMethod.Get){
      context.call.respond(HttpStatusCode.OK,
        ServerResponse(200, HttpStatusCode.OK.description, SaveManager.listOfSaves())
      )

      return
    }

    val data = BlocklyService.checkAndDeserialize(context.call.receiveText()) ?: kotlin.run {
      context.call.respond(HttpStatusCode.UnprocessableEntity,
        ServerResponse(422, HttpStatusCode.UnprocessableEntity.description, null as String?)
      )

      return
    }
    when(data.mode){
      "CREATE" -> {
        BlocklyService.addHook(data).apply {
          if (!this) context.call.respond(HttpStatusCode.InternalServerError,
            ServerResponse(500, HttpStatusCode.InternalServerError.description, null as String?)
          )
        }
      }
      "UPDATE" -> {
        if (data.uuid == null) context.call.respond(HttpStatusCode.BadRequest,
          ServerResponse(400, HttpStatusCode.BadRequest.description, "UUID can not be null")
        )
        BlocklyService.updateHook(data).apply{
          if (!this) {
            BlocklyService.addHook(data).apply {
              if (!this) context.call.respond(HttpStatusCode.InternalServerError,
                ServerResponse(500, HttpStatusCode.InternalServerError.description, null as String?)
              )
            }
          }
        }
      }
      "DELETE" -> {
        if (data.uuid == null) context.call.respond(HttpStatusCode.BadRequest,
          ServerResponse(400, HttpStatusCode.BadRequest.description, "UUID can not be null")
        )
        BlocklyService.deleteHook(data).apply {
          when(this){
            -1 -> context.call.respond(HttpStatusCode.BadRequest,
              ServerResponse(400, HttpStatusCode.BadRequest.description, "Undefined UUID"))

            -2 -> context.call.respond(HttpStatusCode.InternalServerError,
              ServerResponse(500, HttpStatusCode.InternalServerError.description, "Get metadata failed"))

            -3 -> context.call.respond(HttpStatusCode.InternalServerError,
              ServerResponse(500, HttpStatusCode.InternalServerError.description, null as String?))
            else -> {}
          }

          if (this < 0) return
        }
      }
      else -> context.call.respond(HttpStatusCode.BadRequest,
        ServerResponse(400, HttpStatusCode.BadRequest.description, null as String?)
      )
    }

//    BlocklyService.trigger()
    context.call.respond(HttpStatusCode.OK,
      ServerResponse(200, HttpStatusCode.OK.description, null as String?)
    )
  }
}