package net.diyigemt.arona.entity

/**
 * 保存trainer指令的覆盖信息
 * @author diyigemt
 * @since 1.0.9
 */
@kotlinx.serialization.Serializable
data class TrainerOverride(
  val type: OverrideType,
  val name: String,
  val value: String
) {
  enum class OverrideType() {
    IMAGE, // 发送图片
    RAW, // 原生别名
    CODE // mirai-code
  }
}