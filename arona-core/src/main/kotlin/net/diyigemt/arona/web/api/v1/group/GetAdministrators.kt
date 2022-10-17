package net.diyigemt.arona.web.api.v1.group

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.responseMessage
import net.mamoe.mirai.console.data.ValueDescription
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

/**
 *@Author hjn
 *@Create 2022/10/16
 */
object GetAdministrators : Worker{
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    val map = mutableMapOf<String, MutableMap<String, Any>>()
    kotlin.runCatching {
      AronaConfig::class.declaredMemberProperties.filter {
        it.hasAnnotation<ValueDescription>()
      }.forEach {
        val data = mutableMapOf<String, Any>()
        val invoke = it.getter.invoke(AronaConfig)!!
        data["value"] = invoke
        val first = it.annotations.filter { an -> an is ValueDescription }.first() as ValueDescription
        data["desc"] = first.value
        map[it.name] = data
      }
      AronaConfig.managerGroup
    }.onSuccess {
      context.call.respond(responseMessage(map))
    }.onFailure {
      context.call.respond(responseMessage(fail()))
    }
  }
}