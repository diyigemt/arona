package net.diyigemt.arona.quartz

import net.diyigemt.arona.Arona
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.util.ActivityUtil
import org.quartz.InterruptableJob
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.text.SimpleDateFormat
import java.util.*

object ActivityNotify: InitializedFunction() {
  private const val ActivityNotifyJobKey = "ActivityNotify"
  private const val ActivityNotifyOneHour = "ActivityNotifyOneHour"
  private const val ActivityKey = "activity"
  private const val ActivityServerKey = "server"
  override fun init() {
    // 每天早上8点触发
    QuartzProvider.createCronTask(ActivityNotifyJob::class.java, "0 0 8 * * ? *", ActivityNotifyJobKey, ActivityNotifyJobKey)
  }

  class ActivityNotifyJob: Job {
    override fun execute(context: JobExecutionContext?) {
      var jp = ActivityUtil.fetchJPActivity()
      if (jp.first.isEmpty() and jp.second.isEmpty()) {
        jp = ActivityUtil.fetchJPActivityFromJP()
      }
      val en = ActivityUtil.fetchENActivity()
      val alertListJP = mutableListOf<Activity>()
      val alertListEN = mutableListOf<Activity>()
      val activeJP = jp.first
        .filter {
          filterActive(it, alertListJP)
        }
      val pendingJP = jp.second
        .filter {
          filterPending(it)
        }
      val activeEN = en.first
        .filter {
          filterActive(it, alertListEN)
        }
      val pendingEN = en.second
        .filter {
          filterPending(it)
        }
      val jpMessage = ActivityUtil.constructMessage(activeJP to pendingJP)
      val enMessage = ActivityUtil.constructMessage(activeEN to pendingEN)
      Arona.sendMessage("arona的防侠预警(日服)\n$jpMessage")
      Arona.sendMessage("arona的防侠预警(国际服)\n$enMessage")
      if (alertListJP.isNotEmpty()) {
        val instance = Calendar.getInstance()
        instance.set(Calendar.HOUR_OF_DAY, 22)
        QuartzProvider.createSingleTask(
          ActivityNotifyOneHourJob::class.java,
          instance.time,
          "${ActivityNotifyOneHour}-jp",
          ActivityNotifyOneHour,
          mapOf(ActivityKey to alertListJP, ActivityServerKey to true)
        )
      }
      if (alertListEN.isNotEmpty()) {
        val instance = Calendar.getInstance()
        val h = extraHAndD(alertListEN[0], active = false).first
        instance.set(Calendar.HOUR_OF_DAY, instance.get(Calendar.HOUR_OF_DAY) + h - 1)
        QuartzProvider.createSingleTask(
          ActivityNotifyOneHourJob::class.java,
          instance.time,
          "${ActivityNotifyOneHour}-en",
          ActivityNotifyOneHour,
          mapOf(ActivityKey to alertListEN, ActivityServerKey to false)
        )
      }
    }

    private fun filterPending(activity: Activity): Boolean {
      val extra = extraHAndD(activity, active = false)
      val h = extra.first
      val d = extra.second
      return d * 24 + h < 48
    }

    private fun filterActive(activity: Activity, list: MutableList<Activity>): Boolean {
      val extra = extraHAndD(activity)
      val h = extra.first
      val d = extra.second
      if (d == 0) {
        list.add(activity)
      }
      return d * 24 + h < 48
    }

    private fun extraHAndD(activity: Activity, active: Boolean = true): Pair<Int, Int> {
      val now = Calendar.getInstance()
      val pattern = if (active) "d天H小时后结束" else "d天H小时后开始"
      now.time = SimpleDateFormat(pattern).parse(activity.time)
      val d = when(now.get(Calendar.DAY_OF_MONTH)) {
        in (0 .. 15) -> now.get(Calendar.DAY_OF_MONTH)
        else -> 0
      }
      val h = now.get(Calendar.HOUR)
      return h to d
    }
  }

  class ActivityNotifyOneHourJob: InterruptableJob {
    override fun execute(context: JobExecutionContext?) {
      val activity = context?.jobDetail?.jobDataMap?.get(ActivityKey) ?: return
      val server = context?.jobDetail?.jobDataMap?.get(ActivityServerKey) ?: return
      val activityString = (activity as List<Activity>)
        .map { at -> "${at.content}\n" }
        .reduceOrNull { prv, cur -> prv + cur }
      val serverString = if ((server as Boolean)) "日服" else "国际服"
      Arona.sendMessage("arona的防侠预警(${serverString})\n" +
        "$activityString" +
        "将会在5小时后结束")
    }

    override fun interrupt() {
      Arona.warning("interrupt")
    }

  }

}