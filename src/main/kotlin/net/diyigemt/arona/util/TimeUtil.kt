package net.diyigemt.arona.util

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

object TimeUtil {

  fun calcTime(now: Date, time: Date, future: Boolean = false): String {
    val hour = floor(((time.time - now.time) / 1000 / 60 / 60).toDouble())
    val day = floor(hour / 24).toInt()
    val leftHour = (hour - day * 24).toInt()
    return "${day}天${leftHour}小时后" + if (future) "开始" else "结束"
  }

}