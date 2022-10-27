package net.diyigemt.arona.web.api.v1

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import net.diyigemt.arona.Arona
import net.diyigemt.arona.web.api.v1.message.FriendContact
import net.diyigemt.arona.web.api.v1.message.GroupContact

/**
 *@Author hjn
 *@Create 2022/10/22
 */
object Contacts : Worker{
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    val res = ContactsList(mutableListOf(), mutableListOf())

    kotlin.runCatching {
      Arona.arona.groups.forEach{
        res.groups.add(GroupContact(it.id, it.name))
      }

      Arona.arona.friends.forEach {
        res.friends.add(FriendContact(it.id, it.nick, it.remark))
      }
    }.onSuccess {
      context.call.respond(responseMessage(res))
    }.onFailure {
      it.printStackTrace()
      context.call.respond(fail())
    }
  }

  @Serializable
  data class ContactsList(
    val groups : MutableList<GroupContact>,
    val friends : MutableList<FriendContact>
  )
}