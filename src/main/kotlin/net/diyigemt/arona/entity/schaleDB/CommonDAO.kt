package net.diyigemt.arona.entity.schaleDB

/**
 *@Author hjn
 *@Create 2022/8/18
 * 部分无用信息的分支被注释，如需要请取消注释
 */

data class CommonDAO(
//  val GachaGroup : List<GachaGroup>,
  val regions : List<Regions>,
//  val changelog : List<ChangeLog>
)

data class GachaGroup(
  val Id : Int,
  val Icon : String,
  val NameEn : String,
  val NameJp : String,
  val Rarity : String,
  val ItemList : List<List<Int>>
)

data class Regions(
  val abbreviation : String,
  val NameEn : String,
  val NameJp : String,
  val NameKr : String,
  val NameTw : String,
  val NameCn : String,
  val studentlevel_max : Int,
  val weaponlevel_max : Int,
  val bondlevel_max : Int,
  val gear1_max : Int,
  val gear2_max : Int,
  val gear3_max : Int,
  val campaign_max : Int,
  val events : List<Int>,
  val event_max : Int,
  val event_701_max : Int,
  val event_701_challenge_max : Int,
  val commission_max : Int,
  val bounty_max : Int,
  val schooldungeon_max : Int,
  val current_gacha : List<CurrentGacha>,
  val current_events : List<CurrentEvents>,
  val current_raid : List<CurrentRaid>
)

data class CurrentGacha(
  val characters : List<Int>,
  val start : Long,
  val end : Long
)

data class CurrentEvents(
  val event : Int,
  val start : Long,
  val end : Long
)

data class CurrentRaid(
  val raid : Int,
  val terrain : String,
  val start : Long,
  val end : Long
)

data class ChangeLog(
  val date : String,
  val contents : List<String>
)