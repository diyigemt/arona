package net.diyigemt.arona.web.database

/**
 *@Author hjn
 *@Create 2022/10/22
 */
data class DBJobResult(
  val code : Int,
  val message : String,
  val data : String? = null
)
