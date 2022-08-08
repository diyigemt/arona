package net.diyigemt.arona.quartz

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaNotifyConfig
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ActivityType
import net.diyigemt.arona.entity.ServerLocale
import net.diyigemt.arona.service.AronaQuartzService
import net.diyigemt.arona.util.ActivityUtil
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.message.code.MiraiCode
import org.quartz.InterruptableJob
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import java.text.SimpleDateFormat
import java.util.*

object ActivityNotify: AronaQuartzService {
  private const val ActivityNotifyJobKey = "ActivityNotify"
  private const val ActivityNotifyDataInitKey = "init"
  private const val ActivityNotifyOneHour = "ActivityNotifyOneHour"
  private const val ActivityKey = "activity"
  private const val MaintenanceKey = "maintenance"
  override lateinit var jobKey: JobKey
  override val id: Int = 12
  override val name: String = "活动推送"
  override var enable: Boolean = true
  class ActivityNotifyJob: Job {
    override fun execute(context: JobExecutionContext?) {
      val jp: Pair<List<Activity>, List<Activity>> = ActivityUtil.fetchJPActivity()
      val en = ActivityUtil.fetchENActivity()
      val alertListJP = mutableListOf<Activity>()
      val alertListEN = mutableListOf<Activity>()
      val filterJP = jp.first
        .filter {
          filterActive(it, alertListJP)
        } to jp.second
        .filter {
          filterPending(it)
        }
      val filterEN = en.first
        .filter {
          filterActive(it, alertListEN)
        } to en.second
        .filter {
          filterPending(it)
        }
      insertAlert(alertListJP)
      insertAlert(alertListEN)
      // 初始化不显示信息
      val init = context?.mergedJobDataMap?.getBoolean(ActivityNotifyDataInitKey) ?: false
      if (init) return
      val jpMessage = ActivityUtil.constructMessage(filterJP)
      val enMessage = ActivityUtil.constructMessage(filterEN)
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
      val dropActivities = activity.filter { isMidnightEndActivity(it) }
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
      if ((d == 0) && isMaintenanceActivity(activity)) {
        insertMaintenanceAlert(activity, h)
      }
      return doFilter(d, h)
    }

    private fun filterActive(activity: Activity, list: MutableList<Activity>): Boolean {
      val extra = extraHAndD(activity)
      val h = extra.first
      val d = extra.second
      if (d == 0) {
        list.add(activity)
      }
      return doFilter(d, h)
    }

    private fun doFilter(d: Int, h: Int): Boolean = when(AronaNotifyConfig.notifyType) {
      NotifyType.ALL -> true
      NotifyType.ONLY_24H -> d * 24 + h < 24
      NotifyType.ONLY_48H -> d * 24 + h < 48
    }

    private fun extraHAndD(activity: Activity, active: Boolean = true): Pair<Int, Int> {
      val tmp1 = activity.time.substringBefore("天")
      val d = tmp1.toInt()
      val h = activity.time.substringAfter("天").substringBefore("小时").toInt()
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
      val serverName = if (server) "日服" else "国际服"
      if (maintenance != null) {
        Arona.sendMessage("距离${serverName}维护还有1小时")
      }
      // 只有维护信息时
      if (activity.isEmpty()) return
      val activityString = activity
        .map { at -> "${at.content}\n" }
        .reduceOrNull { prv, cur -> prv + cur }
      val serverString = MiraiCode.deserializeMiraiCode(if (server) AronaNotifyConfig.notifyStringJP else AronaNotifyConfig.notifyStringEN)
      val settingDropTime = if (AronaNotifyConfig.dropNotify <= 3) (AronaNotifyConfig.dropNotify + 24) else AronaNotifyConfig.dropNotify
      val endTime = if (server || isMidnightEndActivity(activity[0])) {
        27 - settingDropTime
      } else {
        1
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

  // 判断是不是双倍掉落(一般在3点结束,其实总力战也是,所以放一起了)
  private fun isMidnightEndActivity(activity: Activity): Boolean = activity.type in (ActivityType.N2_3 .. ActivityType.JOINT_EXERCISES)

  private fun isMaintenanceActivity(activity: Activity): Boolean = activity.type == ActivityType.MAINTENANCE

  private fun isJPServer(activity: List<Activity>) = isJPServer(activity[0])

  private fun isJPServer(activity: Activity) = activity.serverLocale == ServerLocale.JP

  override fun init() {
    registerService()
  }

  override fun enableService() {
    // 每天早上8点触发
    jobKey = QuartzProvider.createCronTask(
      ActivityNotifyJob::class.java,
      "0 0 ${AronaNotifyConfig.everyDayHour} * * ? *",
      ActivityNotifyJobKey,
      ActivityNotifyJobKey
    ).first
    QuartzProvider.createSimpleDelayJob(20) {
      QuartzProvider.triggerTaskWithData(jobKey, mapOf(ActivityNotifyDataInitKey to true))
    }
  }
}

// 每日防侠提醒类型
enum class NotifyType {
  ALL, ONLY_24H, ONLY_48H
}