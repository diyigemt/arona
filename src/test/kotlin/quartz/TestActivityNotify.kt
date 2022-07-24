package org.example.mirai.plugin.quartz

import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.util.ActivityUtil
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.Calendar

class TestActivityNotify {

  @Test
  fun testA() {
    var jp = ActivityUtil.fetchJPActivityFromCN()
    if (jp.first.isEmpty() and jp.second.isEmpty()) {
      jp = ActivityUtil.fetchJPActivityFromJP()
    }
    val en = ActivityUtil.fetchENActivity()
    val activeJP = jp.first
      .filter {
        filterActivity(it)
      }
    val pendingJP = jp.second
      .filter {
        filterActivity(it, active =  false)
      }
    val activeEN = en.first
      .filter {
        filterActivity(it, jp = false)
      }
    val pendingEN = en.second
      .filter {
        filterActivity(it, active = false)
      }
    val jpMessage = ActivityUtil.constructMessage(activeJP to pendingJP)
    val enMessage = ActivityUtil.constructMessage(activeEN to pendingEN)
    println("arona的防侠预警(日服)\n$jpMessage")
    println("arona的防侠预警(国际服)\n$enMessage")
  }

  private fun filterActivity(activity: Activity, active: Boolean = true, jp: Boolean = true): Boolean {
    val now = Calendar.getInstance()
    val pattern = if (active) "d天H小时后结束" else "d天H小时后开始"
    now.time = SimpleDateFormat(pattern).parse(activity.time)
    val d = when(now.get(Calendar.DAY_OF_MONTH)) {
      in (0 .. 15) -> now.get(Calendar.DAY_OF_MONTH)
      else -> 0
    }
    val h = now.get(Calendar.HOUR)
    if (d == 0 && active) createAlertNotify(activity, h, jp)
    return d * 24 + h < 48
  }

  private fun createAlertNotify(activity: Activity, last: Int, isJp: Boolean) {

  }

}