package net.diyigemt.arona.web.api.v1.config

import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.web.api.v1.ServerResponse
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.message.ContentUnit
import net.mamoe.mirai.console.data.ValueDescription
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

/**
 *@Author hjn
 *@Create 2022/10/17
 */
object GetAronaConfig : Worker{
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    val res : MutableMap<String, ContentUnit<Any>> = mutableMapOf()
    kotlin.runCatching {
      AronaConfig.javaClass.kotlin.declaredMemberProperties.forEach {
        res[it.name] = ContentUnit(it.get(AronaConfig)!!, it.findAnnotation<ValueDescription>()!!.value)
      }
    }.onSuccess {
      context.call.respond(Gson().toJson(ServerResponse(200, HttpStatusCode.OK.description, res)))
    }.onFailure {
      it.printStackTrace()
      context.call.respond(fail())
    }
  }
}