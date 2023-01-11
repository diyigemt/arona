package net.diyigemt.arona.web.api.v1

import io.ktor.http.*
import net.mamoe.mirai.Bot
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import net.diyigemt.arona.config.GlobalConfigProvider
import net.diyigemt.arona.interfaces.ConfigReader
import net.diyigemt.arona.web.api.v1.message.FriendContact
import net.diyigemt.arona.web.api.v1.message.GroupContact

/**
 *@Author hjn
 *@Create 2022/10/22
 */
object Contacts : Worker, ConfigReader{
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    val res = ContactsList(mutableListOf(), mutableListOf())

    kotlin.runCatching {
      // TODO 将数据结构与bot绑定
      val bots = GlobalConfigProvider.getBotConfig().map { it.bot }
      val bot = Bot.getInstanceOrNull(bots[0]) ?: throw RuntimeException("bot: ${bots[0]} not found")
      bot.groups.forEach{
        res.groups.add(GroupContact(it.id, it.name))
      }
      bot.friends.forEach {
        res.friends.add(FriendContact(it.id, it.nick, it.remark))
      }
    }.onSuccess {
      context.call.respond(responseMessage(res))
    }.onFailure {
      it.printStackTrace()
      context.call.respond(HttpStatusCode.InternalServerError, fail())
    }
  }

  @Serializable
  data class ContactsList(
    val groups : MutableList<GroupContact>,
    val friends : MutableList<FriendContact>
  )

  override val configPrefix: String = ""
}