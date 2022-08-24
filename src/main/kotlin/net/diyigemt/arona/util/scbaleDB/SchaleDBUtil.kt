package net.diyigemt.arona.util.scbaleDB

import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ActivityType
import net.diyigemt.arona.entity.schaleDB.*
import net.diyigemt.arona.util.ActivityUtil
import net.diyigemt.arona.util.scbaleDB.factories.CalendarFactory
import java.time.ZoneId
import java.util.*

/**
 *@Author hjn
 *@Create 2022/8/18
 */
object SchaleDBUtil {
  lateinit var commonItem : CommonDAO
  lateinit var studentItem : StudentDAO
  lateinit var localizationItem : LocalizationDAO
  lateinit var raidItem : RaidDAO
  var birthdayList : MutableList<Birthday> = mutableListOf()

  fun getGlobalEventData(): Pair<MutableList<Activity>, MutableList<Activity>> = getData(ServerType.GLOBAL)

  fun getJPEventData(): Pair<MutableList<Activity>, MutableList<Activity>> = getData(ServerType.JP)

  private fun getData(type : ServerType) : Pair<MutableList<Activity>, MutableList<Activity>>{
    val active: MutableList<Activity> = mutableListOf()
    val pending: MutableList<Activity> = mutableListOf()

    //Characters
    for (item in commonItem.regions[type.ordinal].current_gacha) {
      ActivityUtil.doInsert(
        Calendar.getInstance().time,
        Date(item.start * 1000),
        Date(item.end * 1000),
        active,
        pending,
        CalendarFactory.getCharacterLocalizationName(item.characters),
        contentSourceJP = if (type == ServerType.JP) ActivityUtil.ActivityJPSource.SCHALE_DB else ActivityUtil.ActivityJPSource.GAME_KEE,
        type0 = if (type == ServerType.GLOBAL) ActivityType.PICK_UP else null
      )
    }

    //Events
    for (item in commonItem.regions[type.ordinal].current_events) {
      ActivityUtil.doInsert(
        Calendar.getInstance().time,
        Date(item.start * 1000),
        Date(item.end * 1000),
        active,
        pending,
        CalendarFactory.getEventLocalizationName(item.event),
        contentSourceJP = if (type == ServerType.JP) ActivityUtil.ActivityJPSource.SCHALE_DB else ActivityUtil.ActivityJPSource.GAME_KEE,
        type0 = if (type == ServerType.GLOBAL) ActivityType.ACTIVITY else null
      )
    }

    //Raids
    for (item in commonItem.regions[type.ordinal].current_raid) {
      val raid = CalendarFactory.getRaidById(item.raid)
      //未开放的活动不予显示
      if(raid == null || !raid.IsReleased[0]) continue
      ActivityUtil.doInsert(
        Calendar.getInstance().time,
        Date(item.start * 1000),
        Date(item.end * 1000),
        active,
        pending,
        raid.NameCn,
        contentSourceJP = if (type == ServerType.JP) ActivityUtil.ActivityJPSource.SCHALE_DB else ActivityUtil.ActivityJPSource.GAME_KEE,
        type0 = if (type == ServerType.GLOBAL) ActivityType.DECISIVE_BATTLE else null
      )
    }

    //Birthday
    if (birthdayList.isEmpty()) SchaleDBDataSyncService.BirthdayJob().getBirthdayList()
    for (item in birthdayList){
      ActivityUtil.doInsert(
        Calendar.getInstance().time,
        Date.from(item.date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
        Date.from(item.date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
        active,
        pending,
        item.name + "的生日",
        contentSourceJP = if (type == ServerType.JP) ActivityUtil.ActivityJPSource.SCHALE_DB else ActivityUtil.ActivityJPSource.GAME_KEE,
        type0 = if (type == ServerType.GLOBAL) ActivityType.BIRTHDAY else null
      )
    }

    return active to pending
  }

  enum class ServerType{
    JP,
    GLOBAL
  }
}