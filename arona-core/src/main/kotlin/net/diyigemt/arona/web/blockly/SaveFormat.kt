package net.diyigemt.arona.web.blockly

import kotlinx.serialization.Serializable
import net.diyigemt.arona.Arona
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.console.util.SemVersion

/**
 *@Author hjn
 *@Create 2023/1/3
 * 当2.0.0之后新加变量必须使用可空类型，为了版本兼容
 */

@Serializable
data class Meta(
  val version: SemVersion = Arona.version,
  val projectName: String = "Untiled",
  val resPath: String = ""
)