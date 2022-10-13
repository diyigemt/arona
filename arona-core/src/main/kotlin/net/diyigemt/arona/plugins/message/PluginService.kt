package net.diyigemt.arona.plugins.message

import net.diyigemt.arona.plugins.PluginManager

/**
 *@Author hjn
 *@Create 2022/10/13
 */
@Suppress("UNCHECKED_CAST")
interface PluginService<T> {
  val pluginID : String

  fun init() = PluginManager.registerService(this.pluginID, this as PluginService<Any>)

  fun serviceHandler(event : T)
}