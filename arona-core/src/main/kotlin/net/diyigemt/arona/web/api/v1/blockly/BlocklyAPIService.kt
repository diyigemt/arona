package net.diyigemt.arona.web.api.v1.blockly

import com.squareup.moshi.Types
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import net.diyigemt.arona.util.MoshiUtil
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.message.ServerResponse
import net.diyigemt.arona.web.blockly.BlocklyService
import net.diyigemt.arona.web.blockly.ListSaves
import net.diyigemt.arona.web.blockly.SaveManager

/**
 *@Author hjn
 *@Create 2022/12/26
 */
object BlocklyAPIService: Worker {
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    if (context.call.request.httpMethod == HttpMethod.Get){
      val type = Types.newParameterizedType(List::class.java, ListSaves::class.java)
      val type2 = Types.newParameterizedType(ServerResponse::class.java, type)
      val json = MoshiUtil.reflect.adapter<ServerResponse<List<ListSaves>>>(type2)
        .toJson(ServerResponse(200, HttpStatusCode.OK.description, SaveManager.listOfSaves()))
      context.call.respond(HttpStatusCode.OK, json)

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
      else -> context.call.respond(HttpStatusCode.BadRequest,
        ServerResponse(400, HttpStatusCode.BadRequest.description, null as String?)
      )
    }

    BlocklyService.trigger()
    context.call.respond(HttpStatusCode.OK,
      ServerResponse(200, HttpStatusCode.OK.description, null as String?)
    )
  }
}