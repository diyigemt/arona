package net.diyigemt.arona.entity

@kotlinx.serialization.Serializable
data class ImageResult(
  val name: String = "",
  val path: String = "",
  val hash: String = "",
  val type: Int = 1,
)
