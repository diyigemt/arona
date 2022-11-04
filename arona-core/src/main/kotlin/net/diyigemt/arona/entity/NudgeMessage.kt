package net.diyigemt.arona.entity

@kotlinx.serialization.Serializable
data class NudgeMessage(
  val message: String,
  val weight: Int = 1
)
