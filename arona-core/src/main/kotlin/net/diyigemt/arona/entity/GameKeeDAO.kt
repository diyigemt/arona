package net.diyigemt.arona.entity

/**
 *@Author hjn
 *@Create 2022/7/22
 */
data class GameKeeDAO(
  val code : Int,
  val msg : String,
  val data : List<Data>,
  val meta : Meta
)

data class Data(
  val id : Int,
  var title : String,
  val link_url : String,
  val picture : String,
  val description : String,
  val begin_at : Long,
  val end_at : Long,
  val importance : Int,
  val count_down : Int,
  val pub_area : String
)

data class Meta(
  val request_id : String,
  val trace_id : String
)
