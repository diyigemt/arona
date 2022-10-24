package net.diyigemt.arona.web.api.v1.commit

import com.squareup.moshi.Types
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import net.diyigemt.arona.annotations.Const
import net.diyigemt.arona.util.MoshiUtil
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.responseMessage
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.starProjectedType

/**
 *@Author hjn
 *@Create 2022/10/17
 */
object CommitManager :Worker {
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    val body = context.call.receiveText()
    val resMap : MutableMap<String, Boolean> = mutableMapOf()

    kotlin.runCatching {
      val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
      val jsonMap = MoshiUtil.reflect.adapter<Map<String, Any>>(type).fromJson(body)!!
      jsonMap.forEach {
        val res = parse(it.key)
        kotlin.runCatching {
          val configClass = Class.forName("net.diyigemt.arona.config." + res[0]).kotlin
          val property = configClass.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>().find { property ->
            property.name == res[1]
          }!!
          val value = when(it.value::class){
            Double::class -> {
              if (property.returnType == Int::class.starProjectedType) (it.value as Double).toInt()
              else (it.value as Double).toLong()
            }
            else -> {it.value}
          }
          if(property.annotations.contains(Const())) throw Exception("${property.name} can not be modified.")
          property.setter.call(configClass.objectInstance, value)
        }.onSuccess { _ ->
          resMap[it.key] = true
        }.onFailure { e ->
          if (e !is Exception) e.printStackTrace()
          resMap[it.key] = false
        }
      }

      context.call.respond(responseMessage(resMap))
    }.onFailure {
      it.printStackTrace()
      context.call.respond(fail())
    }
  }

  private fun parse(data : String) : List<String> = data.split(".")
}