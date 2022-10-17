package net.diyigemt.arona.web.api.v1.message

import kotlinx.serialization.Serializable

/**
 *@Author hjn
 *@Create 2022/10/16
 */
@Serializable
data class ListMessage<T>(
  val value : List<T>
)