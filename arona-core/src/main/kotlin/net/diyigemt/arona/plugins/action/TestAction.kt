package net.diyigemt.arona.plugins.action

import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.diyigemt.arona.Arona
import net.diyigemt.arona.plugins.event.webuievent.CommitEvent
import net.diyigemt.arona.plugins.message.ResponseMessage
import net.mamoe.mirai.console.plugin.id

/**
 *@Author hjn
 *@Create 2022/10/14
 */
class TestAction : BaseAction<CommitEvent>{
  override val actionName =  "TestAction"
  override fun actionHandler(event: CommitEvent) {
    Arona.info("TestAction: ${event.action}")
    event.action = Json.encodeToString(ResponseMessage(Arona.id, "ResponseAction", HttpStatusCode.OK.value, "hello there"))
  }
}