package net.diyigemt.arona.plugins.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.diyigemt.arona.Arona
import net.diyigemt.arona.plugins.PluginMessage
import net.mamoe.mirai.console.plugin.id

/**
 *@Author hjn
 *@Create 2022/10/13
 */
@Serializable
data class ResponseMessage(
  override val pluginID: String,
  val code : Int,
  val message : String
) : PluginMessage
