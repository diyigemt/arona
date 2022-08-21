package net.diyigemt.arona.entity.schaleDB

import net.diyigemt.arona.util.scbaleDB.factories.BaseFactory

/**
 *@Author hjn
 *@Create 2022/8/18
 */

class StudentDAO : ArrayList<StudentDAOItem>(){
  fun getStudentNameById(studentID : Int) : String? {
    for (item in this){
      if (item.Id == studentID) return item.Name
    }

    return null
  }
}

data class StudentDAOItem(
    val AccuracyPoint: Int,
    val AmmoCost: Int,
    val AmmoCount: Int,
    val ArmorType: String,
    val ArtistName: String,
    val AttackPower1: Int,
    val AttackPower100: Int,
    val BirthDay: String,
    val Birthday: String,
    val BulletType: String,
    val CharHeightImperial: String,
    val CharHeightMetric: String,
    val CharacterAge: String,
    val CharacterSSRNew: String,
    val CharacterVoice: String,
    val Club: String,
    val CollectionBG: String,
    val CollectionTexture: String,
    val Cover: Boolean,
    val CriticalDamageRate: Int,
    val CriticalPoint: Int,
    val DefaultOrder: Int,
    val DefensePower1: Int,
    val DefensePower100: Int,
    val DevName: String,
    val DodgePoint: Int,
    val Equipment: List<String>,
    val FamilyName: String,
    val FamilyNameRuby: Any,
    val FavorAlts: List<Int>,
    val FavorItemTags: List<String>,
    val FavorItemUniqueTags: List<String>,
    val FavorStatType: List<String>,
    val FavorStatValue: List<List<Int>>,
    val FurnitureInteraction: List<Int>,
    val Gear: Gear,
    val HealPower1: Int,
    val HealPower100: Int,
    val Hobby: String,
    val Id: Int,
    val IndoorBattleAdaptation: Int,
    val IsLimited: Int,
    val IsReleased: List<Boolean>,
    val MaxHP1: Int,
    val MaxHP100: Int,
    val MemoryLobby: Int,
    val Name: String,
    val OutdoorBattleAdaptation: Int,
    val PathName: String,
    val PersonalName: String,
    val Position: String,
    val ProfileIntroduction: String,
    val Range: Int,
    val RegenCost: Int,
    val School: String,
    val SchoolYear: String,
    val SkillExMaterial: List<List<Int>>,
    val SkillExMaterialAmount: List<List<Int>>,
    val SkillMaterial: List<List<Int>>,
    val SkillMaterialAmount: List<List<Int>>,
    val Skills: List<Skill>,
    val SquadType: String,
    val StabilityPoint: Int,
    val StarGrade: Int,
    val StreetBattleAdaptation: Int,
    val SummonIds: List<Int>,
    val TacticRole: String,
    val Weapon: Weapon,
    val WeaponImg: String,
    val WeaponType: String
)

data class Gear(
    val Desc: String,
    val Icon: String,
    val Name: String,
    val Released: List<Boolean>,
    val StatType: List<String>,
    val StatValue: List<List<Int>>,
    val TierUpMaterial: List<List<Int>>,
    val TierUpMaterialAmount: List<List<Int>>
)

data class Skill(
    val Cost: List<Int>,
    val Desc: String,
    val Icon: String,
    val Name: String,
    val Parameters: List<List<String>>,
    val SkillType: String,
    val Stat: List<String>,
    val SummonStat: List<String>,
    val SummonStatCoefficient: List<List<Int>>
)

data class Weapon(
    val AdaptationType: String,
    val AdaptationValue: Int,
    val AttackPower1: Int,
    val AttackPower100: Int,
    val Desc: String,
    val HealPower1: Int,
    val HealPower100: Int,
    val MaxHP1: Int,
    val MaxHP100: Int,
    val Name: String,
    val StatLevelUpType: String
)