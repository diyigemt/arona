package net.diyigemt.arona.entity

/**
 * 对消息类型起反应的回复消息
 */
@kotlinx.serialization.Serializable
data class ReplyRule(
  val rule: ReplyMessageMatchTree,
  val messages: List<ReplyMessage>,
  val messageType: ReplyMessageType
)

@kotlinx.serialization.Serializable
data class ReplyMessage(
  val message: String,
  val weight: Int = 1
)

@kotlinx.serialization.Serializable
data class ReplyMessageMatchTree(
  val left: ReplyMessageMatchTree? = null,
  val right: ReplyMessageMatchTree? = null,
  val condition: ReplyMessageMatchCondition? = null,
  val type: ReplyMessageMatchType? = null,
  val value: String? = null
)

@kotlinx.serialization.Serializable
enum class ReplyMessageType {
  MESSAGE, NUDGE
}

@kotlinx.serialization.Serializable
enum class ReplyMessageMatchType {
  SUFFIX, PREFIX, CONTAINS, ACCURATE, SENDER, GROUP
}

@kotlinx.serialization.Serializable
enum class ReplyMessageMatchCondition {
  AND, OR, NOT
}