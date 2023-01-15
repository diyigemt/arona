package net.diyigemt.arona.web.api.v1

import io.ktor.http.*
import net.mamoe.mirai.Bot
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import net.diyigemt.arona.config.GlobalConfigProvider
import net.diyigemt.arona.interfaces.ConfigReader
import net.diyigemt.arona.web.entity.BotContact
import net.diyigemt.arona.web.entity.toFriend
import net.diyigemt.arona.web.entity.toGroup
import net.diyigemt.arona.web.entity.toMember

/**
 *@Author hjn
 *@Create 2022/10/22
 */
object Contacts : Worker, ConfigReader{
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    when(context.call.request.httpMethod) {
      HttpMethod.Get -> {
        //TODO
        kotlin.runCatching {
          // TODO 将数据结构与bot绑定
          val bots = GlobalConfigProvider.getBotConfig().map { it.bot }
          Bot.getInstanceOrNull(bots[0]) ?: throw RuntimeException("bot: ${bots[0]} not found")
        }.onSuccess {
          context.call.respond(responseMessage(BotContact(
            it.id,
            it.friends.map { friend -> friend.toFriend() },
            it.groups.map { group -> group.toGroup() }
          )))
        }.onFailure {
          it.printStackTrace()
          context.call.respond(HttpStatusCode.InternalServerError, fail())
        }
      }

      HttpMethod.Post -> {
        kotlin.runCatching {
          val groupId = context.call.parameters["group"]!!
          val bots = GlobalConfigProvider.getBotConfig().map { it.bot }
          // TODO 将数据结构与bot绑定
          val bot = Bot.getInstanceOrNull(bots[0]) ?: throw RuntimeException("bot: ${bots[0]} not found")
          bot.getGroup(groupId.toLong())!!
        }.onSuccess {
          context.call.respond(responseMessage(
            it.members.map { normalMember -> normalMember.toMember() }
          ))
        }.onFailure {
          it.printStackTrace()
          context.call.respond(HttpStatusCode.InternalServerError, fail())
        }
      }
    }
  }

  override val configPrefix: String = ""
}