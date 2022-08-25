package net.diyigemt.arona.entity

import kotlinx.serialization.Serializable

@Serializable
data class ServerResponse<T>(
  val status: Int,
  val message: String,
  val data: T
)
