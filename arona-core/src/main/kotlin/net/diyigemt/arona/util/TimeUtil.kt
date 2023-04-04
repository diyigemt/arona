package net.diyigemt.arona.util

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

object TimeUtil {
  private val TIME_PREFIX = listOf("今天", "明天", "后天")
  private val DEFAULT_TIME_FORMAT = SimpleDateFormat("MM月dd日 HH:mm")
  fun today(): Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

  fun calcTime(time: Date, future: Boolean = false): String {
    val suffix = if (future) "开始" else "结束"
    val calendar = Calendar.getInstance()
    calendar.time = time
    if (calendar.get(Calendar.MINUTE) == 59) {
      calendar.set(Calendar.MINUTE, 0)
      calendar.add(Calendar.HOUR_OF_DAY, 1)
    }
    return DEFAULT_TIME_FORMAT.format(calendar.time) + suffix
  }

  fun translateTimeMoreReadable(timeString: String, future: Boolean = false): String {
    val result = buildNowAndTime(timeString)
    val calendar = result.first
    val now = result.second
    val activityHour = calendar.get(Calendar.HOUR_OF_DAY)
    val diff = calcDiffDayAndHour(timeString)
    var day = diff.first
    val hour = diff.second
    // 计算相差多少天
    var hourDiff = hour - (24 - now.get(Calendar.HOUR_OF_DAY))
    if (hourDiff < 0) {
      hourDiff += 24
    } else {
      day += 1
    }
    if (day in (0 .. 2)) {
      return "${TIME_PREFIX[day]}${activityHour}点" + if (future) "开始" else "结束"
    }
    return timeString
  }

  fun calcDiffDayAndHour(timeString: String): Pair<Int, Int> {
    val result = buildNowAndTime(timeString)
    val calendar = result.first
    val now = result.second
    val hour = floor(((calendar.time.time - now.time.time) / 1000 / 60 / 60).toDouble())
    val day = floor(hour / 24).toInt()
    val leftHour = (hour - day * 24).toInt()
    return day to leftHour
  }

  private fun buildNowAndTime(timeString: String): Pair<Calendar, Calendar> {
    val date = DEFAULT_TIME_FORMAT.parse(timeString)
    val calendar = Calendar.getInstance()
    calendar.time = date
    val now = Calendar.getInstance()
    calendar.set(Calendar.YEAR, now.get(Calendar.YEAR))
    calendar.set(Calendar.MONTH, now.get(Calendar.MONTH))
    return calendar to now
  }

}