package net.diyigemt.arona.web.api.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.diyigemt.arona.web.api.v1.message.ServerResponse

/**
 *@Author hjn
 *@Create 2022/10/16
 */
interface Worker {
  suspend fun worker(context : PipelineContext<Unit, ApplicationCall>){
    context.call.response.header("Content-Type", "application/json")
  }

  val json: Json
    get() = Json { encodeDefaults = true }

  fun fail() = json.encodeToString(ServerResponse(500, HttpStatusCode.InternalServerError.description, null as String?))

  fun noContent() = json.encodeToString(ServerResponse(204, HttpStatusCode.NoContent.description, null as String?))
}

inline fun <reified T> responseMessage(json: T, code: Int = 200) = Json.encodeToString(
  ServerResponse(code, HttpStatusCode.OK.description, json)
)