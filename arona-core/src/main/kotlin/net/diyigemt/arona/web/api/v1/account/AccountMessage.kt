package net.diyigemt.arona.web.api.v1.account

import kotlinx.serialization.Serializable

/**
 *@Author hjn
 *@Create 2022/10/16
 */
@Serializable
data class AccountMessage(
  val qq : Long
)