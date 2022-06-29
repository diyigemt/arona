package net.diyigemt.arona.quartz

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaNotifyConfig
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
  private const val ActivityNotifyDataInitKey = "init"
  private const val ActivityNotifyOneHour = "ActivityNotifyOneHour"
  private const val ActivityKey = "activity"
  private const val ActivityServerKey = "server"
  override fun init() {
    // 全局启用标志
    if (!AronaNotifyConfig.enable) return
    // 每天早上8点触发
    QuartzProvider.createCronTask(ActivityNotifyJob::class.java, "0 0 ${AronaNotifyConfig.everyDayHour} * * ? *", ActivityNotifyJobKey, ActivityNotifyJobKey)
    QuartzProvider.triggerTaskWithData(ActivityNotifyJobKey, ActivityNotifyJobKey, mapOf(ActivityNotifyDataInitKey to true))
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
      val init = context?.mergedJobDataMap?.getBoolean(ActivityNotifyDataInitKey) ?: false
      // 初始化不显示信息
      if (!init) {
        if (AronaNotifyConfig.enableEveryDay) {
          if (AronaNotifyConfig.enableJP) {
            Arona.sendMessage("${AronaNotifyConfig.notifyStringJP}\n$jpMessage")
          }
          if (AronaNotifyConfig.enableEN) {
            Arona.sendMessage("${AronaNotifyConfig.notifyStringEN}\n$enMessage")
          }
        }
      }
      if (alertListJP.isNotEmpty() && AronaNotifyConfig.enableJP) {
        val instance = Calendar.getInstance()
        instance.set(Calendar.HOUR_OF_DAY, AronaNotifyConfig.jpHour)
        QuartzProvider.createSingleTask(
          ActivityNotifyOneHourJob::class.java,
          instance.time,
          "${ActivityNotifyOneHour}-jp",
          ActivityNotifyOneHour,
          mapOf(ActivityKey to alertListJP, ActivityServerKey to true)
        )
      }
      if (alertListEN.isNotEmpty() && AronaNotifyConfig.enableEN) {
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
      val activity = context?.mergedJobDataMap?.get(ActivityKey) ?: return
      val server = context.mergedJobDataMap?.getBoolean(ActivityServerKey) ?: return
      val activityString = (activity as List<Activity>)
        .map { at -> "${at.content}\n" }
        .reduceOrNull { prv, cur -> prv + cur }
      val serverString = if (server) AronaNotifyConfig.notifyStringJP else AronaNotifyConfig.notifyStringEN
      Arona.sendMessage("${serverString}\n" +
        "$activityString" +
        if (server) "将会在${27 - AronaNotifyConfig.jpHour}小时后结束" else "将会在1小时后结束"
      )
    }

    override fun interrupt() {
      Arona.warning("interrupt")
    }

  }

}