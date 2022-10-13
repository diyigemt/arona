package net.diyigemt.arona.plugins.message

import kotlinx.serialization.Serializable
import net.diyigemt.arona.plugins.PluginMessage

/**
 *@Author hjn
 *@Create 2022/10/13
 */
@Serializable
data class SingleMessage(
  override val pluginID: String,
  val message : String
) : PluginMessage
