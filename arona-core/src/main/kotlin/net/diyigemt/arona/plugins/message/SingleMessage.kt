package net.diyigemt.arona.plugins.message

import kotlinx.serialization.Serializable

/**
 *@Author hjn
 *@Create 2022/10/13
 */
@Serializable
data class SingleMessage(
  override val pluginID: String,
  val action : String,
  val message : String
) : PluginMessage
