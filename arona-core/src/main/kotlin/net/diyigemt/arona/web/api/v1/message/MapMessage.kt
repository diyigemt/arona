package net.diyigemt.arona.web.api.v1.message

import kotlinx.serialization.Serializable

/**
 *@Author hjn
 *@Create 2022/10/17
 */
@Serializable
data class MapMessage <T, K>(
  val value : Map<T, K>
)
