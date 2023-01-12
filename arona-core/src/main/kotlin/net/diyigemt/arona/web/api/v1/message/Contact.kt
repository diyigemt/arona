package net.diyigemt.arona.web.api.v1.message

import kotlinx.serialization.Serializable

/**
 *@Author hjn
 *@Create 2022/10/22
 */
@Serializable
data class Contact(
  val id : Long,
  val name : String,
  val remark : String
)

@Serializable
data class GroupContact(
  val id : Long,
  val name : String
)
