package net.diyigemt.arona.web.api.v1.data

import com.squareup.moshi.Types
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import net.diyigemt.arona.util.MoshiUtil
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.message.DBJob
import net.diyigemt.arona.web.api.v1.message.Properties
import net.diyigemt.arona.web.api.v1.message.ServerResponse
import net.diyigemt.arona.web.api.v1.message.Task
import net.diyigemt.arona.web.database.DBOptionService

/**
 *@Author hjn
 *@Create 2022/10/22
 */
object Data : Worker {
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    kotlin.runCatching {
      val json = when(context.call.request.httpMethod){
        HttpMethod.Get -> DBJob(properties = Properties(task = Task("SELECT")))
        else -> MoshiUtil.reflect.adapter(DBJob::class.java).fromJson(context.call.receiveText())!!
      }
      json.properties.db = context.call.parameters["dataBase"]!!
      json.properties.table = context.call.parameters["table"]!!
      val res = DBOptionService.addTask(json)
      val type = Types.newParameterizedType(ServerResponse::class.java, Any::class.java)
      context.call.respond(MoshiUtil.reflect.adapter<ServerResponse<out Any>>(type).serializeNulls().toJson(res))
    }.onFailure {
      it.printStackTrace()
      context.call.respond(HttpStatusCode.InternalServerError, fail())
    }
  }
}