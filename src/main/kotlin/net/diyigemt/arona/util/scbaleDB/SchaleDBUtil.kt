package net.diyigemt.arona.util.scbaleDB

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ActivityType
import net.diyigemt.arona.entity.schaleDB.CommonDAO
import net.diyigemt.arona.entity.schaleDB.LocalizationDAO
import net.diyigemt.arona.entity.schaleDB.RaidDAO
import net.diyigemt.arona.entity.schaleDB.StudentDAO
import net.diyigemt.arona.util.ActivityUtil
import net.diyigemt.arona.util.scbaleDB.factories.CalendarFactory
import org.jsoup.Jsoup
import java.net.URLEncoder
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

    return active to pending
  }
}