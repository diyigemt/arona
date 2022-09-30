package net.diyigemt.arona.util.scbaleDB

import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ActivityType
import net.diyigemt.arona.entity.ServerLocale
import net.diyigemt.arona.entity.schaleDB.*
import net.diyigemt.arona.util.ActivityUtil
import net.diyigemt.arona.util.scbaleDB.factories.CalendarFactory
import java.time.ZoneId
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

/**
 *@Author hjn
 *@Create 2022/8/18
 */
object SchaleDBUtil {
  var commonItem : CommonDAO = CommonDAO()
  var studentItem : StudentDAO = StudentDAO()
  var localizationItem : LocalizationDAO = LocalizationDAO()
  var raidItem : RaidDAO = RaidDAO()
  var birthdayList : MutableList<Birthday> = mutableListOf()

  fun getGlobalEventData(): Pair<MutableList<Activity>, MutableList<Activity>> = getData(ServerLocale.GLOBAL)

  fun getJPEventData(): Pair<MutableList<Activity>, MutableList<Activity>> = getData(ServerLocale.JP)

  fun getENBirthdayData(): List<Activity> = getBirthdayData(ServerLocale.GLOBAL)

  fun getJPBirthdayData(): List<Activity> = getBirthdayData(ServerLocale.JP)

  private fun getData(type : ServerLocale) : Pair<MutableList<Activity>, MutableList<Activity>>{
    val active: MutableList<Activity> = mutableListOf()
    val pending: MutableList<Activity> = mutableListOf()
    val now = Calendar.getInstance().time
    val function = when(type) {
      ServerLocale.JP -> ActivityUtil::insertJpActivity
      ServerLocale.GLOBAL -> ActivityUtil::insertEnActivity
    }
    val source = when(type) {
      ServerLocale.JP -> ActivityUtil.ActivityJPSource.SCHALE_DB
      ServerLocale.GLOBAL -> ActivityUtil.ActivityENSource.SCHALE_DB
    }
    //Characters
    for (item in commonItem.regions[type.ordinal].current_gacha) {
      val name = CalendarFactory.getCharacterLocalizationName(item.characters)
      if (name.isBlank()) {
        continue
      }
      function.call(
        now,
        Date(item.start * 1000),
        Date(item.end * 1000),
        active,
        pending,
        CalendarFactory.getCharacterLocalizationName(item.characters),
        source,
        ActivityType.PICK_UP
      )
    }

    //Events
    for (item in commonItem.regions[type.ordinal].current_events) {
      val name = CalendarFactory.getEventLocalizationName(item.event)
      if (name.isBlank()) {
        continue
      }
      function.call(
        now,
        Date(item.start * 1000),
        Date(item.end * 1000),
        active,
        pending,
        CalendarFactory.getEventLocalizationName(item.event),
        source,
        ActivityType.ACTIVITY
      )
    }

    //Raids
    for (item in commonItem.regions[type.ordinal].current_raid) {
      val raid = CalendarFactory.getRaidById(item.raid)
      //未开放的活动不予显示
      if(raid == null || !raid.IsReleased[0]) continue
      val terrain = raid.Terrain.firstOrNull().let {
        return@let if (it.isNullOrBlank()) ""
        else it.replace("Outdoor", "屋外")
          .replace("Street", "市街")
          .replace("Indoor", "室内")
      }
      function.call(
        now,
        Date(item.start * 1000),
        Date(item.end * 1000),
        active,
        pending,
        "${raid.NameCn}($terrain)",
        source,
        ActivityType.DECISIVE_BATTLE
      )
    }

    //Birthday
    if (birthdayList.isEmpty()) SchaleDBDataSyncService.BirthdayJob().getBirthdayList()
    for (item in birthdayList){
      function.call(
        now,
        Date.from(item.date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
        Date.from(item.date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
        active,
        pending,
        item.name + "的生日",
        source,
        ActivityType.BIRTHDAY
      )
    }

    return active to pending
  }

  private fun getBirthdayData(locale : ServerLocale): List<Activity> {
    val list = mutableListOf<Activity>()
    val function = when(locale) {
      ServerLocale.JP -> ActivityUtil::insertJpActivity
      ServerLocale.GLOBAL -> ActivityUtil::insertEnActivity
    }
    val source = when(locale) {
      ServerLocale.JP -> ActivityUtil.ActivityJPSource.SCHALE_DB
      ServerLocale.GLOBAL -> ActivityUtil.ActivityENSource.SCHALE_DB
    }
    if (birthdayList.isEmpty()) SchaleDBDataSyncService.BirthdayJob().getBirthdayList()
    val now = Calendar.getInstance().time
    for (item in birthdayList){
      function.call(
        now,
        Date.from(item.date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
        Date.from(item.date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
        mutableListOf<Activity>(),
        list,
        item.name + "的生日",
        source,
        ActivityType.BIRTHDAY
      )
    }
    return list
  }
}