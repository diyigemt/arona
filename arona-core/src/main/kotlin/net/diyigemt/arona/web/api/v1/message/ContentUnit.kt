package net.diyigemt.arona.web.api.v1.message

import kotlinx.serialization.Serializable

/**
 *@Author hjn
 *@Create 2022/10/17
 */
@Serializable
data class ContentUnit<T>(
  val value : T,
  val description : String
)