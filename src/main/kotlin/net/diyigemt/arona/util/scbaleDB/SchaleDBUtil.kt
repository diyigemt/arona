package net.diyigemt.arona.util.scbaleDB

import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ActivityType
import net.diyigemt.arona.entity.schaleDB.*
import net.diyigemt.arona.util.ActivityUtil
import net.diyigemt.arona.util.scbaleDB.factories.CalendarFactory
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

  fun getGlobalEventData(): Pair<MutableList<Activity>, MutableList<Activity>> {
    val active: MutableList<Activity> = mutableListOf()
    val pending: MutableList<Activity> = mutableListOf()

    //Characters
    for (item in commonItem.regions[1].current_gacha) {
      ActivityUtil.doInsert(
        Calendar.getInstance().time,
        Date(item.start * 1000),
        Date(item.end * 1000),
        active,
        pending,
        CalendarFactory.getCharacterLocalizationName(item.characters),
        type0 = ActivityType.PICK_UP
      )
    }

    //Events
    for (item in commonItem.regions[1].current_events) {
      ActivityUtil.doInsert(
        Calendar.getInstance().time,
        Date(item.start * 1000),
        Date(item.end * 1000),
        active,
        pending,
        CalendarFactory.getEventLocalizationName(item.event),
        type0 = ActivityType.ACTIVITY
      )
    }

    //Raids
    for (item in commonItem.regions[1].current_raid) {
      ActivityUtil.doInsert(
        Calendar.getInstance().time,
        Date(item.start * 1000),
        Date(item.end * 1000),
        active,
        pending,
        CalendarFactory.getRaidLocalizationName(item.raid),
        type0 = ActivityType.DECISIVE_BATTLE
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
        type0 = ActivityType.BIRTHDAY
      )
    }

    return active to pending
  }
}