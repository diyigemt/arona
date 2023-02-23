package net.diyigemt.arona.web.api.v1.mirai

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import net.diyigemt.arona.config.GlobalConfigProvider
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.message.ServerResponse
import net.diyigemt.arona.web.api.v1.responseMessage
import net.diyigemt.arona.web.entity.BotFriend
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.nameCardOrNick

/**
 *@Author hjn
 *@Create 2023/2/23
 */
object BotsAPI: Worker {
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    kotlin.runCatching {
      val res: MutableList<BotFriend> = mutableListOf()
      GlobalConfigProvider.getBotConfig().map { it.bot }.forEach {
        Bot.getInstanceOrNull(it).apply {
          if (this != null) res.add(BotFriend(it, this.nameCardOrNick, ""))
        }
      }
      return@runCatching res
    }.onFailure {
      it.printStackTrace()
      context.call.respond(ServerResponse(500, HttpStatusCode.InternalServerError.description, ""))
    }.onSuccess { context.call.respond(responseMessage(it)) }
  }
}