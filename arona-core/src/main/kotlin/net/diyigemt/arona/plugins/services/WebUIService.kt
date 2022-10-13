package net.diyigemt.arona.plugins.services

import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.diyigemt.arona.Arona
import net.diyigemt.arona.plugins.event.webuievent.CommitEvent
import net.diyigemt.arona.plugins.message.PluginService
import net.diyigemt.arona.plugins.message.ResponseMessage
import net.mamoe.mirai.console.plugin.id

/**
 *@Author hjn
 *@Create 2022/10/12
 */
class WebUIService : PluginService<CommitEvent> {
  override val pluginID = "net.diyigemt.arona-webui"
  override fun serviceHandler(event: CommitEvent) {
    Arona.info("WebUIService: ${event.action}")
    event.action = Json.encodeToString(ResponseMessage(Arona.id, HttpStatusCode.OK.value, "hello there"))
  }
}