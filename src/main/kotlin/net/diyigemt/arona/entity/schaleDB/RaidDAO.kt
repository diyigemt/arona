package net.diyigemt.arona.entity.schaleDB

import net.diyigemt.arona.db.DB
import net.diyigemt.arona.db.DataBaseProvider
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

/**
 *@Author hjn
 *@Create 2022/8/20
 */
data class RaidDAO(
  var Raid: List<Raid>,
//    val SeasonRewardGlobal: List<SeasonRewardGlobal>,
//    val SeasonRewardJp: List<SeasonRewardJp>,
//    val TimeAttack: List<TimeAttack>,
//    val TimeAttackRules: List<TimeAttackRule>,
//    val WorldRaid: List<WorldRaid>
) : BaseDAO{
  fun getRaidNameById(raidID : Int) : String?{
    for(item in Raid){
      if(item.Id == raidID) return item.NameCn
    }

    return null
  }

  fun isRaidReleased(raidID: Int) : Boolean{
    for(item in Raid){
      if(item.Id == raidID) return item.IsReleased[0]
    }

    return false
  }

  override fun sendToDataBase() {
    DataBaseProvider.query(DB.DATA.ordinal) { net.diyigemt.arona.db.data.schaledb.Raid.deleteAll() }
    for (item in Raid){
      DataBaseProvider.query(DB.DATA.ordinal) {
        net.diyigemt.arona.db.data.schaledb.Raid.insert {
          it[Id] = item.Id
          it[IsReleased] = item.IsReleased.first()
          it[NameCn] = item.NameCn
          it[CurrentJPN] = ""
          it[CurrentGLB] = ""
        }
      }
    }
  }

  override fun <T : BaseDAO> toModel(dao: T): T {
    dao as RaidDAO
    dao.Raid = mutableListOf()

    val query = kotlin.runCatching {
      DataBaseProvider.query(DB.DATA.ordinal) {
        net.diyigemt.arona.db.data.schaledb.Raid.selectAll().toList()
      }
    }.getOrNull()?: mutableListOf()

    for (item in query){
      val id = item.getOrNull(net.diyigemt.arona.db.data.schaledb.Raid.Id)!!
      val isReleased = item.getOrNull(net.diyigemt.arona.db.data.schaledb.Raid.IsReleased)!!
      val nameCN = item.getOrNull(net.diyigemt.arona.db.data.schaledb.Raid.NameCn)!!
      dao.Raid = dao.Raid.plus(Raid(Id = id, IsReleased = mutableListOf(isReleased, isReleased), NameCn = nameCN))
    }

    return dao
  }
}

data class Raid(
  val ArmorType: String = "",
  val BulletType: String = "",
  val BulletTypeInsane: String = "",
  val EnemyList: List<List<Int>> = mutableListOf(),
  val Faction: String = "",
  val Icon: String = "",
  val IconBG: String = "",
  var Id: Int,
  var IsReleased: List<Boolean>,
  val IsReleasedInsane: List<Boolean> = mutableListOf(),
  var NameCn: String,
  val NameEn: String = "",
  val NameJp: String = "",
  val NameKr: String = "",
  val NameTh: String = "",
  val NameTw: String = "",
  val PathName: String = "",
  val ProfileCn: String = "",
  val ProfileEn: String = "",
  val ProfileJp: String = "",
  val ProfileKr: String = "",
  val ProfileTh: String = "",
  val ProfileTw: String = "",
  val RaidSkill: List<RaidSkill> = mutableListOf(),
  val Terrain: List<String> = mutableListOf()
)

data class SeasonRewardGlobal(
    val End: Int,
    val RaidId: Int,
    val Rewards: List<List<Any>>,
    val Season: Int,
    val Start: Int,
    val Terrain: String
)

data class SeasonRewardJp(
    val End: Int,
    val RaidId: Int,
    val Rewards: List<List<Any>>,
    val Season: Int,
    val Start: Int,
    val Terrain: String
)

data class TimeAttack(
    val ArmorType: String,
    val BulletType: String,
    val DungeonType: String,
    val EnemyLevel: List<Int>,
    val Formations: List<Formation>,
    val Icon: String,
    val Id: Int,
    val IsReleased: List<Boolean>,
    val MaxDifficulty: Int,
    val Rules: List<List<Long>>,
    val Terrain: String
)

data class TimeAttackRule(
    val DescEn: String,
    val DescJp: String,
    val DescKr: String,
    val Icon: String,
    val Id: Long,
    val NameEn: String,
    val NameJp: String,
    val NameKr: String
)

data class WorldRaid(
    val ArmorType: String,
    val BulletType: String,
    val EnemyList: List<List<Int>>,
    val IconBG: String,
    val Id: Int,
    val IsReleased: List<Boolean>,
    val Level: List<Int>,
    val NameCn: String,
    val NameEn: String,
    val NameJp: String,
    val NameKr: String,
    val NameTh: String,
    val NameTw: String,
    val PathName: String,
    val RaidSkill: List<RaidSkillX>,
    val Rewards: List<List<List<Double>>>,
    val Terrain: List<String>,
    val WorldBossHP: Long
)

data class RaidSkill(
    val ATGCost: Int,
    val DescCn: String,
    val DescEn: String,
    val DescJp: String,
    val DescKr: String,
    val DescTh: String,
    val DescTw: String,
    val Icon: String,
    val Id: String,
    val MinDifficulty: Int,
    val NameCn: String,
    val NameEn: String,
    val NameJp: String,
    val NameKr: String,
    val NameTh: String,
    val NameTw: String,
    val ParametersCn: List<List<String>>,
    val ParametersEn: List<List<String>>,
    val ParametersJp: List<List<String>>,
    val ParametersKr: List<List<String>>,
    val ParametersTh: List<List<String>>,
    val ParametersTw: List<List<String>>,
    val SkillType: String
)

data class Formation(
    val EnemyList: List<Int>,
    val Grade: List<Int>,
    val Id: Int,
    val Level: List<Int>
)

data class RaidSkillX(
    val ATGCost: Int,
    val DescCn: Any,
    val DescEn: String,
    val DescJp: String,
    val DescKr: String,
    val DescTh: Any,
    val DescTw: Any,
    val Icon: String,
    val Id: String,
    val MinDifficulty: Int,
    val NameCn: Any,
    val NameEn: String,
    val NameJp: String,
    val NameKr: String,
    val SkillType: String
)