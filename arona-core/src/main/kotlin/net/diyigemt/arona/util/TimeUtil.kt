package net.diyigemt.arona.util

import java.util.*
import kotlin.math.floor

object TimeUtil {
  private val TIME_PREFIX = listOf("今天", "明天", "后天")
  fun today(): Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

  fun calcTime(now: Date, time: Date, future: Boolean = false): String {
    val hour = floor(((time.time - now.time) / 1000 / 60 / 60).toDouble())
    val day = floor(hour / 24).toInt()
    val leftHour = (hour - day * 24).toInt()
    return "${day}天${leftHour}小时后" + if (future) "开始" else "结束"
  }

  fun translateTimeMoreReadable(timeString: String, future: Boolean = false): String {
    val tmp1 = timeString.substringBefore("天")
    var day = tmp1.toInt()
    val hour = timeString.substringAfter("天").substringBefore("小时").toInt()
    val now = Calendar.getInstance()
    val nowHour = now.get(Calendar.HOUR_OF_DAY)

    var hourDiff = hour - (24 - nowHour)
    if (hourDiff < 0) {
      hourDiff += 24
    } else {
      day += 1
    }
    if (day < 3) {
      return "${TIME_PREFIX[day]}${hourDiff}时" + if (future) "开始" else "结束"
    }
    return timeString
  }

}