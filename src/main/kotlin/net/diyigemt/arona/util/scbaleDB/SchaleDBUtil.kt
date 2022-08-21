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
  private const val common = "https://lonqie.github.io/SchaleDB/data/common.min.json"
  private const val student = "https://lonqie.github.io/SchaleDB/data/cn/students.min.json"
  private const val localization = "https://lonqie.github.io/SchaleDB/data/cn/localization.json"
  private const val raid = "https://lonqie.github.io/SchaleDB/data/raids.min.json"

  fun getGlobalEventData(): Pair<MutableList<Activity>, MutableList<Activity>> {
    val commonData: CommonDAO = getCommonData()
    val active: MutableList<Activity> = mutableListOf()
    val pending: MutableList<Activity> = mutableListOf()

    //Characters
    for (item in commonData.regions[1].current_gacha) {
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
    for (item in commonData.regions[1].current_events) {
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
    for (item in commonData.regions[1].current_raid) {
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

  private fun getCommonData(): CommonDAO {
    val res = Jsoup.connect(common)
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .get()
      .text()

    return Gson().fromJson(res, CommonDAO::class.java)
  }

  fun getStudentData(): StudentDAO {
    val res = Jsoup.connect(student)
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .header("Content-Type", "application/json;charset=UTF-8")
      .maxBodySize(0)
      .ignoreContentType(true)
      .execute()
      .body()

    return Gson().fromJson(res, StudentDAO::class.java)
  }

  fun getLocalizationData(): LocalizationDAO {
    val res = Jsoup.connect(localization)
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .timeout(5000)
      .get()
      .text()

    return Gson().fromJson(res, LocalizationDAO::class.java)
  }

  fun getRaidData() : RaidDAO{
    val res = Jsoup.connect(raid)
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .header("Content-Type", "application/json;charset=UTF-8")
      .maxBodySize(0)
      .ignoreContentType(true)
      .execute()
      .body()

    return Gson().fromJson(res,RaidDAO::class.java)
  }
}