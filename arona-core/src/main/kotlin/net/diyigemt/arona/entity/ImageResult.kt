package net.diyigemt.arona.entity

import java.io.File

@kotlinx.serialization.Serializable
data class ImageResult(
  val id: Int = 0,
  val name: String = "",
  val path: String = "",
  val hash: String = "",
  val type: Int = 1,
)

data class ImageRequestResult(
  val list: List<ImageResult> = listOf(),
  val file: File? = null
)

const val FuzzyImageResult = 0