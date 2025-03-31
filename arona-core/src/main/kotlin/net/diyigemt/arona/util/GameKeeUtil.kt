package net.diyigemt.arona.util

import com.google.gson.Gson
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ActivityType
import net.diyigemt.arona.entity.GameKeeDAO
import net.diyigemt.arona.entity.ServerLocale
import org.jsoup.Jsoup
import java.util.*

/**
 *@Author hjn
 *@Create 2022/7/21
 */
object GameKeeUtil {
  private const val url = "https://ba.gamekee.com/v1/activity/query"
  fun getEventData(server: ServerLocale) : Pair<MutableList<Activity>, MutableList<Activity>>{
    val res = NetworkUtil.request(Jsoup.connect(url))
      .header("game-alias", "ba")
      .data("active_at", ((Calendar.getInstance().timeInMillis) / 1000).toString())
      .get()
      .text()
    return analyze(Gson().fromJson(res, GameKeeDAO::class.java), server)
  }

  private fun analyze(json : GameKeeDAO, server: ServerLocale) : Pair<MutableList<Activity>, MutableList<Activity>>{
    val active : MutableList<Activity> = mutableListOf()
    val pending : MutableList<Activity> = mutableListOf()
    val method = when(server){
      ServerLocale.GLOBAL -> ActivityUtil::insertEnActivity
      ServerLocale.JP -> ActivityUtil::insertJpActivity
      ServerLocale.CN -> ActivityUtil::insertCnActivity
    }
    val source = when(server){
      ServerLocale.GLOBAL -> ActivityUtil.ActivityENSource.GAME_KEE
      ServerLocale.JP -> ActivityUtil.ActivityJPSource.GAME_KEE
      ServerLocale.CN -> ActivityUtil.ActivityCNSource.GAME_KEE
    }

    for(i in json.data){
      if(i.pub_area == server.serverName){
        method.call(
          Calendar.getInstance().time,
          Date(i.begin_at * 1000),
          Date(i.end_at * 1000),
          active,
          pending,
          i.title,
          source,
          ActivityType.NULL
        )
      }
    }

    return active to pending
  }
}
