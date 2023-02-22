package net.diyigemt.arona.web.api.v1.reply

import com.squareup.moshi.Types
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.reply.ContentItem
import net.diyigemt.arona.db.reply.MessageGroup
import net.diyigemt.arona.db.reply.MessageGroups
import net.diyigemt.arona.util.MoshiUtil
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.message.ServerResponse
import net.diyigemt.arona.web.api.v1.responseMessage
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

/**
 *@Author hjn
 *@Create 2023/2/21
 */
object MessageGroup: Worker {
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    val type = Types.newParameterizedType(List::class.java, ContentItem::class.java)
    when(context.call.request.httpMethod) {
      HttpMethod.Get -> kotlin.runCatching {
        val res: MutableList<MessageGroup> = mutableListOf()
        DataBaseProvider.query {
          MessageGroups.selectAll().forEach { row ->
            res.add(
              MessageGroup(
                row[MessageGroups.id].value,
                MoshiUtil.reflect.adapter<List<ContentItem>>(type).fromJson(row[MessageGroups.content])!!,
                row[MessageGroups.weight],
                row[MessageGroups.labels].let { raw ->
                  raw.substring(1 until raw.length - 1).split(",").map { it.toInt() }
                }
              )
            )
          }
        }
        return@runCatching res
      }.onFailure {
        it.printStackTrace()
        context.call.respond(ServerResponse(500, HttpStatusCode.InternalServerError.description, ""))
      }.onSuccess { context.call.respond(responseMessage(it)) }
      HttpMethod.Post -> kotlin.runCatching {
        val receive = context.call.receiveText()
        val intListType = Types.newParameterizedType(List::class.java, Int::class.javaObjectType)
        when(context.call.parameters["method"]) {
          "create" -> {
            val json = json.decodeFromString(ResponseMessageGroup.serializer(), receive).group
            DataBaseProvider.query {
              MessageGroups.insert {
                it[content] = MoshiUtil.reflect.adapter<List<ContentItem>>(type).toJson(json.content)
                it[weight] = json.weight
                it[labels] = MoshiUtil.reflect.adapter<List<Int>>(intListType).toJson(json.label)
              }
            }
          }
          "update" -> {
            val json = json.decodeFromString(ResponseMessageGroup.serializer(), receive).group
            DataBaseProvider.query {
              MessageGroups.update({MessageGroups.id eq json.id}) {
                it[content] = MoshiUtil.reflect.adapter<List<ContentItem>>(type).toJson(json.content)
                it[weight] = json.weight
                it[labels] = MoshiUtil.reflect.adapter<List<Int>>(intListType).toJson(json.label)
              }
            }
          }
          "delete" -> {
            val json = json.decodeFromString(Labels.DeleteRequest.serializer(), receive)
            DataBaseProvider.query {
              MessageGroups.deleteWhere { MessageGroups.id eq json.id }
            }
          }

          else -> context.call.respond(ServerResponse(400, HttpStatusCode.BadRequest.description, ""))
        }
      }.onFailure {
        it.printStackTrace()
        context.call.respond(ServerResponse(500, HttpStatusCode.InternalServerError.description, ""))
      }.onSuccess { context.call.respond(responseMessage("")) }
    }
  }

  @Serializable
  data class ResponseMessageGroup(val group: MessageGroup)
}