package net.diyigemt.arona.entity.schaleDB

import net.diyigemt.arona.db.DB
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.data.schaledb.CurrentData
import net.diyigemt.arona.util.scbaleDB.SchaleDBDataSyncService
import net.diyigemt.arona.util.scbaleDB.SchaleDBUtil
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

/**
 *@Author hjn
 *@Create 2022/8/18
 * 部分无用信息的分支被注释，如需要请取消注释
 */

data class CommonDAO(
//  val GachaGroup : List<GachaGroup>,
  var regions : List<Regions>,
//  val changelog : List<ChangeLog>
) : BaseDAO{
  override fun sendToDataBase(){
    var isJPN = true
    //删表重填
    DataBaseProvider.query(DB.DATA.ordinal) { CurrentData.deleteAll() }
    for (item in regions){
      //characters
      for (chaPool in item.current_gacha){
        for (cha in chaPool.characters){
          DataBaseProvider.query(DB.DATA.ordinal){
            CurrentData.insert {
              it[value] = cha
              it[type] = SchaleDBDataSyncService.DataBaseType.STUDENT.name
              it[server] = item.abbreviation ?: getServer(isJPN)
              it[start] = chaPool.start
              it[end] = chaPool.end
            }
          }
        }
      }

      //events
      for(event in item.current_events){
        DataBaseProvider.query(DB.DATA.ordinal) {
          CurrentData.insert {
            it[value] = event.event
            it[type] = SchaleDBDataSyncService.DataBaseType.EVENT.name
            it[server] = item.abbreviation ?: getServer(isJPN)
            it[start] = event.start
            it[end] = event.end
          }
        }
      }

      //raid
      for(raid in item.current_raid){
        DataBaseProvider.query(DB.DATA.ordinal) {
          CurrentData.insert {
            it[value] = raid.raid
            it[type] = SchaleDBDataSyncService.DataBaseType.RAID.name
            it[server] = item.abbreviation ?: getServer(isJPN)
            it[start] = raid.start
            it[end] = raid.end
          }
        }
      }

      isJPN = isJPN.not()
    }
  }

  override fun <T : BaseDAO> toModel(dao : T) : T{
    dao as CommonDAO
    for (item in dao.regions){
      item.current_gacha = mutableListOf()
      item.current_events = mutableListOf()
      item.current_raid = mutableListOf()
    }
    var res: T

    //JPN
    res = generateServerData(SchaleDBUtil.ServerType.JPN, dao)

    //GLB
    res = generateServerData(SchaleDBUtil.ServerType.GLB, res)

    return res
  }

  private fun <T : BaseDAO> generateServerData(type : SchaleDBUtil.ServerType, dao : T) : T{
    val serverType = type.ordinal
    dao as CommonDAO
    //character
    var query = kotlin.runCatching {
      DataBaseProvider.query(DB.DATA.ordinal) {
        CurrentData.select(
          CurrentData.type eq "STUDENT" and (CurrentData.server eq type.name)
        ).groupBy(CurrentData.start, CurrentData.end).toList()
      }
    }.getOrNull()?: mutableListOf()

    //筛选时间戳创建卡池
    for (item in query){
      dao.regions[serverType].current_gacha = dao.regions[serverType].current_gacha.plusElement(CurrentGacha(mutableListOf(), 0L, 0L))
      val start = item.getOrNull(CurrentData.start)!!
      val end = item.getOrNull(CurrentData.end)!!
      dao.regions[serverType].current_gacha.last().start = start
      dao.regions[serverType].current_gacha.last().end = end
    }

    //向卡池中放卡
    query = kotlin.runCatching {
      DataBaseProvider.query(DB.DATA.ordinal) {
        CurrentData.select(
          CurrentData.type eq "STUDENT" and (CurrentData.server eq type.name)
        ).toList()
      }
    }.getOrNull()?: mutableListOf()

    for (item in query){
      val start = item.getOrNull(CurrentData.start)!!
      val end = item.getOrNull(CurrentData.end)!!
      val value = item.getOrNull(CurrentData.value)!!
      for (pool in dao.regions[serverType].current_gacha){
        if (pool.start == start && pool.end == end){
          pool.characters = pool.characters.plus(value)
        }
      }
    }

    //event
    query = kotlin.runCatching {
      DataBaseProvider.query(DB.DATA.ordinal) {
        CurrentData.select(
          CurrentData.type eq "EVENT" and (CurrentData.server eq type.name)
        ).toList()
      }
    }.getOrNull()?: mutableListOf()

    for (item in query){
      val start = item.getOrNull(CurrentData.start)!!
      val end = item.getOrNull(CurrentData.end)!!
      val value = item.getOrNull(CurrentData.value)!!
      dao.regions[serverType].current_events = dao.regions[serverType].current_events.plus(CurrentEvents(value, start, end))
    }

    //raid
    query = kotlin.runCatching {
      DataBaseProvider.query(DB.DATA.ordinal) {
        CurrentData.select(
          CurrentData.type eq "RAID" and (CurrentData.server eq type.name)
        ).toList()
      }
    }.getOrNull()?: mutableListOf()

    for (item in query){
      val start = item.getOrNull(CurrentData.start)!!
      val end = item.getOrNull(CurrentData.end)!!
      val value = item.getOrNull(CurrentData.value)!!
      dao.regions[serverType].current_raid = dao.regions[serverType].current_raid.plus(CurrentRaid(value,"", start, end))
    }

    return dao
  }
}

data class GachaGroup(
  val Id : Int,
  val Icon : String,
  val NameEn : String,
  val NameJp : String,
  val Rarity : String,
  val ItemList : List<List<Int>>
)

data class Regions(
  val abbreviation : String?,
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
  var current_gacha : List<CurrentGacha>,
  var current_events : List<CurrentEvents>,
  var current_raid : List<CurrentRaid>
)

data class CurrentGacha(
  var characters : List<Int>,
  var start : Long,
  var end : Long
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