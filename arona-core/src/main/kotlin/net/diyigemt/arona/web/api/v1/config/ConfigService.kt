package net.diyigemt.arona.web.api.v1.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import net.diyigemt.arona.config.GlobalConfigProvider
import net.diyigemt.arona.entity.BotGroupConfig
import net.diyigemt.arona.web.api.v1.Worker
import net.diyigemt.arona.web.api.v1.message.ServerResponse

/**
 *@Author hjn
 *@Create 2022/10/17
 */
object ConfigService : Worker{
  override suspend fun worker(context: PipelineContext<Unit, ApplicationCall>) {
    super.worker(context)
    kotlin.runCatching {
      context.call.respond(ServerResponse(200, "OK", Configs()))
    }.onFailure {
      it.printStackTrace()
      context.call.respond(ServerResponse(500, HttpStatusCode.InternalServerError.description, ""))
    }
  }

  @Serializable
  data class Configs(
    val bots: List<BotGroupConfig> = GlobalConfigProvider.getBotConfig(),
    val managerGroup: List<Long> = GlobalConfigProvider.getGroupList(),
  )
}