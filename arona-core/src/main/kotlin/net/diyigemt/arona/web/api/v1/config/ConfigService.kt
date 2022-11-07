package net.diyigemt.arona.web.api.v1.config

import com.squareup.moshi.Types
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import net.diyigemt.arona.util.MoshiUtil
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.message.ContentUnit
import net.diyigemt.arona.web.api.v1.message.ServerResponse
import net.mamoe.mirai.console.data.ValueDescription
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

/**
 *@Author hjn
 *@Create 2022/10/17
 */
object ConfigService : Worker{
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    val res : MutableMap<String, ContentUnit<Any>> = mutableMapOf()
    val config = context.call.parameters["config"]!!
    kotlin.runCatching {
      val targetInstance = Class.forName("net.diyigemt.arona.config.$config").kotlin
      targetInstance.declaredMemberProperties.forEach{
        res[it.name] = ContentUnit(it.getter.call(targetInstance.objectInstance)!!, it.findAnnotation<ValueDescription>()!!.value)
      }
    }.onSuccess {
      val type = Types.newParameterizedType(ContentUnit::class.java, Any::class.java)
      val type2 = Types.newParameterizedType(Map::class.java, String::class.java, type)
      val type3 = Types.newParameterizedType(ServerResponse::class.java, type2)
      val json = MoshiUtil.reflect.adapter<ServerResponse<Map<String, ContentUnit<Any>>>>(type3)
        .toJson(ServerResponse(HttpStatusCode.OK.value, HttpStatusCode.OK.description, res))
      context.call.respond(json)
    }.onFailure {
      when(it){
        is ClassNotFoundException -> context.call.respond(
          ServerResponse(HttpStatusCode.NotFound.value, "No config named: $config found", null as String?)
        )
        else -> {
          it.printStackTrace()
          context.call.respond(fail())
        }
      }
    }
  }
}