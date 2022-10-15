package net.diyigemt.arona.plugins.services

import net.diyigemt.arona.plugins.PluginManager
import net.diyigemt.arona.plugins.action.BaseAction

/**
 *@Author hjn
 *@Create 2022/10/13
 */
@Suppress("UNCHECKED_CAST")
interface PluginService<T> {
  val pluginID : String
  val actionMap : MutableMap<String, BaseAction<Any>>

  fun actionInit()

  fun init(){
    PluginManager.registerService(this.pluginID, this as PluginService<Any>)
    actionInit()
  }

  fun serviceHandler(event : T)
}