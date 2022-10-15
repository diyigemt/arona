package net.diyigemt.arona.plugins.services

import kotlinx.serialization.json.Json
import net.diyigemt.arona.Arona
import net.diyigemt.arona.plugins.action.BaseAction
import net.diyigemt.arona.plugins.action.TestAction
import net.diyigemt.arona.plugins.event.webuievent.CommitEvent
import net.diyigemt.arona.plugins.message.SingleMessage

/**
 *@Author hjn
 *@Create 2022/10/12
 */
class WebUIService : PluginService<CommitEvent> {
  override val pluginID = "net.diyigemt.arona-webui"
  override val actionMap = mutableMapOf<String, BaseAction<Any>>()

  override fun serviceHandler(event: CommitEvent) {
    val res = Json.decodeFromString(SingleMessage.serializer(), event.action)
    actionMap[res.action]?.also {
      it.actionHandler(event)
      return
    }
    Arona.warning("WebUIService: no action handler match ${res.pluginID}")
  }

  override fun actionInit() {
    actionMap.putAll(TestAction().actionInit())
  }
}