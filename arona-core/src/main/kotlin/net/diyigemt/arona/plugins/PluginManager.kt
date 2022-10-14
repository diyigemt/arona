package net.diyigemt.arona.plugins

import kotlinx.serialization.json.Json
import net.diyigemt.arona.Arona
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.plugins.event.webuievent.CommitEvent
import net.diyigemt.arona.plugins.services.PluginService
import net.diyigemt.arona.plugins.message.SingleMessage
import net.diyigemt.arona.plugins.services.WebUIService
import net.mamoe.mirai.event.GlobalEventChannel

/**
 *@Author hjn
 *@Create 2022/10/13
 */
object PluginManager : InitializedFunction(){
  private val map : MutableMap<String, PluginService<Any>?> = mutableMapOf()
  private val json = Json

  override fun init() {
    registerServices()
    messageListener()
  }

  private fun registerServices(){
    WebUIService().init()
  }

  private fun messageListener(){
    GlobalEventChannel.subscribeAlways<CommitEvent> {
      val res = json.decodeFromString(SingleMessage.serializer(), action)
      map[res.pluginID]?.also {
        it.serviceHandler(this)
        return@subscribeAlways
      }
      Arona.warning("MessageListener: Message: $action doesn't match any plugin service. Did you perform a handshake?")
    }
  }

  fun registerService(pluginID: String, self: PluginService<Any>){
    if(map[pluginID] != null){
      Arona.warning("pluginID: $pluginID already registered")
    }else{
      map[pluginID] = self
    }
  }
}