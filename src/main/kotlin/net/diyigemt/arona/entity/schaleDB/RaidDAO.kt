package net.diyigemt.arona.entity.schaleDB

/**
 *@Author hjn
 *@Create 2022/8/20
 */
data class RaidDAO(
    val Raid: List<Raid>,
    val SeasonRewardGlobal: List<SeasonRewardGlobal>,
    val SeasonRewardJp: List<SeasonRewardJp>,
    val TimeAttack: List<TimeAttack>,
    val TimeAttackRules: List<TimeAttackRule>,
    val WorldRaid: List<WorldRaid>
){
  fun getRaidNameById(raidID : Int) : String?{
    for(item in this.Raid){
      if(item.Id == raidID) return item.NameCn
    }

    return null
  }

  fun isRaidReleased(raidID: Int) : Boolean{
    for(item in this.Raid){
      if(item.Id == raidID) return item.IsReleased[0]
    }

    return false
  }
}

data class Raid(
    val ArmorType: String,
    val BulletType: String,
    val BulletTypeInsane: String,
    val EnemyList: List<List<Int>>,
    val Faction: String,
    val Icon: String,
    val IconBG: String,
    val Id: Int,
    val IsReleased: List<Boolean>,
    val IsReleasedInsane: List<Boolean>,
    val NameCn: String,
    val NameEn: String,
    val NameJp: String,
    val NameKr: String,
    val NameTh: String,
    val NameTw: String,
    val PathName: String,
    val ProfileCn: String,
    val ProfileEn: String,
    val ProfileJp: String,
    val ProfileKr: String,
    val ProfileTh: String,
    val ProfileTw: String,
    val RaidSkill: List<RaidSkill>,
    val Terrain: List<String>
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