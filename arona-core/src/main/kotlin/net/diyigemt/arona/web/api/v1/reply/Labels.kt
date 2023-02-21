package net.diyigemt.arona.web.api.v1.reply

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.reply.LabelItem
import net.diyigemt.arona.db.reply.Labels
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.message.ServerResponse
import net.diyigemt.arona.web.api.v1.responseMessage
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

/**
 *@Author hjn
 *@Create 2023/2/19
 */
object Labels: Worker {
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    when(context.call.request.httpMethod) {
      HttpMethod.Get -> context.call.respond(responseMessage(getLabelItems()))
      HttpMethod.Post -> {
        val receive = context.call.receiveText()

        when(context.call.parameters["method"]) {
          "create" -> kotlin.runCatching {
            val json = json.decodeFromString(RequestLabel.serializer(), receive).label
            DataBaseProvider.query {
              Labels.insert {
                it[value] = json.value
                it[weight] = json.weight
              }
            }
          }.onFailure {
            it.printStackTrace()
            context.call.respond(ServerResponse(500, HttpStatusCode.InternalServerError.description, ""))
          }.onSuccess { context.call.respond(responseMessage("")) }
          "update" -> kotlin.runCatching {
            val json = json.decodeFromString(RequestLabel.serializer(), receive).label
            DataBaseProvider.query {
              Labels.update({Labels.id eq json.id}) {
                it[value] = json.value
                it[weight] = json.weight
              }
            }
          }.onFailure {
            it.printStackTrace()
            context.call.respond(ServerResponse(500, HttpStatusCode.InternalServerError.description, ""))
          }.onSuccess { context.call.respond(responseMessage("")) }
          "delete" -> kotlin.runCatching {
            val json = json.decodeFromString(DeleteRequest.serializer(), receive)
            DataBaseProvider.query {
              Labels.deleteWhere { Labels.id eq json.id }
            }
          }.onFailure {
            it.printStackTrace()
            context.call.respond(ServerResponse(500, HttpStatusCode.InternalServerError.description, ""))
          }.onSuccess { context.call.respond(responseMessage("")) }
        }
      }
    }
  }

  private fun getLabelItems(): List<LabelItem> {
    val res: MutableList<LabelItem> = mutableListOf()
    DataBaseProvider.query {
      Labels.selectAll().forEach {
        res.add(LabelItem(it[Labels.id].value, it[Labels.value], it[Labels.weight]))
      }
    }

    return res
  }

  @Serializable
  data class RequestLabel(val label: LabelItem)

  @Serializable
  data class DeleteRequest(val id: Int)
}