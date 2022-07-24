package net.diyigemt.arona.util

import com.google.gson.Gson
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.GameKeeDAO
import org.jsoup.Jsoup
import java.util.*

/**
 *@Author hjn
 *@Create 2022/7/21
 */
object GameKeeUtil {
  private const val url = "https://ba.gamekee.com/v1/activity/query"
  fun getEventData() : Pair<MutableList<Activity>, MutableList<Activity>>{
    val res = Jsoup.connect(url)
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .header("game-alias", "ba")
      .data("active_at", ((Calendar.getInstance().timeInMillis) / 1000).toString())
      .get()
      .text()

    return analyze(Gson().fromJson(res, GameKeeDAO::class.java))
  }

  private fun analyze(json : GameKeeDAO) : Pair<MutableList<Activity>, MutableList<Activity>>{
    val active : MutableList<Activity> = mutableListOf()
    val pending : MutableList<Activity> = mutableListOf()

    for(i in json.data){
      ActivityUtil.doInsert(
        Calendar.getInstance().time,
        Date(i.begin_at * 1000),
        Date(i.end_at * 1000),
        active,
        pending,
        i.title,
        description = i.description,
        contentSourceJP = ActivityUtil.ActivityJPSource.GAME_KEE
      )
    }

    return active to pending
  }
}