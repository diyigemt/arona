package net.diyigemt.arona.web.api.v1

import io.ktor.http.*
import net.mamoe.mirai.Bot
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.GlobalConfigProvider
import net.diyigemt.arona.interfaces.ConfigReader
import net.diyigemt.arona.web.api.v1.message.Contact
import net.diyigemt.arona.web.api.v1.message.GroupContact
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.contact.remarkOrNick

/**
 *@Author hjn
 *@Create 2022/10/22
 */
object Contacts : Worker, ConfigReader{
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    when(context.call.request.httpMethod) {
      HttpMethod.Get -> {
        val res = ContactsList(mutableListOf(), mutableListOf())

        kotlin.runCatching {
          // TODO 将数据结构与bot绑定
          val bots = GlobalConfigProvider.getBotConfig().map { it.bot }
          val bot = Bot.getInstanceOrNull(bots[0]) ?: throw RuntimeException("bot: ${bots[0]} not found")
          bot.groups.forEach{
            res.groups.add(GroupContact(it.id, it.name))
          }
          bot.friends.forEach {
            res.friends.add(Contact(it.id, it.nick, it.remark))
          }
        }.onSuccess {
          context.call.respond(responseMessage(res))
        }.onFailure {
          it.printStackTrace()
          context.call.respond(HttpStatusCode.InternalServerError, fail())
        }
      }

      HttpMethod.Post -> {
        val res: MutableList<Contact> = mutableListOf();
        kotlin.runCatching {
          val groupID = context.call.parameters["group"]!!
          val bots = GlobalConfigProvider.getBotConfig().map { it.bot }
          val bot = Bot.getInstanceOrNull(bots[0]) ?: throw RuntimeException("bot: ${bots[0]} not found")
          bot.getGroup(groupID.toLong())!!.members.forEach {
            res.add(Contact(it.id, it.nick, it.nameCardOrNick))
          }
        }.onSuccess {
          context.call.respond(responseMessage(res))
        }.onFailure {
          it.printStackTrace()
          context.call.respond(HttpStatusCode.InternalServerError, fail())
        }
      }
    }
  }

  @Serializable
  data class ContactsList(
    val groups : MutableList<GroupContact>,
    val friends : MutableList<Contact>
  )

  override val configPrefix: String = ""
}