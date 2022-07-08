package net.diyigemt.arona.quartz

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaNotifyConfig
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ActivityType
import net.diyigemt.arona.entity.ServerLocale
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.util.ActivityUtil
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.message.code.MiraiCode
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
  private const val MaintenanceKey = "maintenance"
  override fun init() {
    // 全局启用标志
    if (!AronaNotifyConfig.enable) return
    // 每天早上8点触发
    QuartzProvider.createCronTask(
      ActivityNotifyJob::class.java,
      "0 0 ${AronaNotifyConfig.everyDayHour} * * ? *",
      ActivityNotifyJobKey,
      ActivityNotifyJobKey
    )
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
      jp.first
        .filter {
          filterActive(it, alertListJP)
        }
      jp.second
        .filter {
          filterPending(it)
        }
      en.first
        .filter {
          filterActive(it, alertListEN)
        }
      en.second
        .filter {
          filterPending(it)
        }
      insertAlert(alertListJP)
      insertAlert(alertListEN)
      // 初始化不显示信息
      val init = context?.mergedJobDataMap?.getBoolean(ActivityNotifyDataInitKey) ?: false
      if (init) return
      val jpMessage = ActivityUtil.constructMessage(jp)
      val enMessage = ActivityUtil.constructMessage(en)
      if (AronaNotifyConfig.enableEveryDay) {
        if (AronaNotifyConfig.enableJP) {
          Arona.sendMessage(
            MessageUtil.deserializeMiraiCodeAndAddString(AronaNotifyConfig.notifyStringJP, "\n$jpMessage")
          )
        }
        if (AronaNotifyConfig.enableEN) {
          Arona.sendMessage(
            MessageUtil.deserializeMiraiCodeAndAddString(AronaNotifyConfig.notifyStringEN, "\n$enMessage")
          )
        }
      }
    }

    private fun doInsert(activity: List<Activity>, h: Int, extraKey: String = "") {
      val now = Calendar.getInstance()
      now.set(Calendar.HOUR_OF_DAY, h)
      now.set(Calendar.MINUTE, 0)
      now.set(Calendar.MILLISECOND, 0)
      val serverJp = isJPServer(activity)
      QuartzProvider.createSingleTask(
        ActivityNotifyOneHourJob::class.java,
        now.time,
        "${ActivityNotifyOneHour}-${if (serverJp) "jp" else "en"}-${h}-${extraKey}",
        ActivityNotifyOneHour,
        mapOf(ActivityKey to activity)
      )
    }
    private fun insertAlert(activity: MutableList<Activity>) {
      if (activity.isEmpty()) return
      val instance = Calendar.getInstance()
      val dropActivities = activity.filter { isDropActivity(it) }
      activity.removeAll(dropActivities)
      // 非双倍掉落提醒
      if (activity.isNotEmpty()) {
        val h = extraHAndD(activity[0]).first
        doInsert(activity, instance.get(Calendar.HOUR_OF_DAY) + h - 1)
      }
      // 双倍掉落提醒
      if (dropActivities.isNotEmpty()) {
        doInsert(dropActivities, AronaNotifyConfig.dropNotify)
      }
    }

    private fun insertMaintenanceAlert(activity: Activity, h: Int) {
      doInsert(listOf(activity), Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + h - 1, extraKey = MaintenanceKey)
    }

    private fun filterPending(activity: Activity): Boolean {
      val extra = extraHAndD(activity, active = false)
      val h = extra.first
      val d = extra.second
      if ((d == 0) and isMaintenanceActivity(activity)) {
        insertMaintenanceAlert(activity, h)
      }
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
      val pattern = if (active) "dd天HH小时后结束" else "dd天HH小时后开始"
      now.time = SimpleDateFormat(pattern).parse(activity.time)
      val d = when(now.get(Calendar.DAY_OF_MONTH)) {
        in (0 .. 15) -> now.get(Calendar.DAY_OF_MONTH)
        else -> 0
      }
      val h = now.get(Calendar.HOUR_OF_DAY)
      return h to d
    }

  }
  @Suppress("UNCHECKED_CAST")
  class ActivityNotifyOneHourJob: InterruptableJob {
    override fun execute(context: JobExecutionContext?) {
      val ac = context?.mergedJobDataMap?.get(ActivityKey) ?: return
      ac as List<Activity>
      if (ac.isEmpty()) return
      val server = isJPServer(ac)
      val activity = ac.toMutableList()
      val maintenance: Activity? = activity
        .filter { isMaintenanceActivity(it) }
        .let {
          if (it.isNotEmpty()) {
            activity.removeAll(it)
            return@let it[0]
          } else {
            return@let null
          }
        }
      val activityString = activity
        .map { at -> "${at.content}\n" }
        .reduceOrNull { prv, cur -> prv + cur }
      val serverName = if (server) "日服" else "国际服"
      val serverString = MiraiCode.deserializeMiraiCode(if (server) AronaNotifyConfig.notifyStringJP else AronaNotifyConfig.notifyStringEN)
      val settingDropTime = if (AronaNotifyConfig.dropNotify <= 3) (AronaNotifyConfig.dropNotify + 24) else AronaNotifyConfig.dropNotify
      val endTime = if (server or isDropActivity(activity[0])) {
        27 - settingDropTime
      } else {
        1
      }
      if (maintenance != null) {
        Arona.sendMessage("距离${serverName}维护还有1小时")
      }
      Arona.sendMessage("${serverString}\n" +
        "$activityString" +
        "将会在${endTime}小时后结束"
      )
    }

    override fun interrupt() {
      Arona.warning("interrupt")
    }
  }

  private fun isDropActivity(activity: Activity): Boolean = activity.type in (ActivityType.N2_3 .. ActivityType.COLLEGE_EXCHANGE_DROP)

  private fun isMaintenanceActivity(activity: Activity): Boolean = activity.type == ActivityType.MAINTENANCE

  private fun isJPServer(activity: List<Activity>) = isJPServer(activity[0])

  private fun isJPServer(activity: Activity) = activity.serverLocale == ServerLocale.JP
}