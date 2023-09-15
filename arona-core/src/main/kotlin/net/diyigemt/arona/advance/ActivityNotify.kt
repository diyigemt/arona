package net.diyigemt.arona.advance

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaNotifyConfig
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ActivityType
import net.diyigemt.arona.entity.ServerLocale
import net.diyigemt.arona.quartz.QuartzProvider
import net.diyigemt.arona.service.AronaQuartzService
import net.diyigemt.arona.util.ActivityUtil
import net.diyigemt.arona.util.MessageUtil
import net.diyigemt.arona.util.TimeUtil.calcDiffDayAndHour
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.message.code.MiraiCode
import org.quartz.InterruptableJob
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import java.io.File
import java.util.*

object ActivityNotify : AronaQuartzService {
  private const val ActivityNotifyJobKey = "ActivityNotify"
  private const val ActivityNotifyDataInitKey = "init"
  private const val ActivityNotifyOneHour = "ActivityNotifyOneHour"
  private const val ActivityKey = "activity"
  private const val NotifyStringKey = "notifyString"
  private const val MaintenanceKey = "maintenance"
  private const val DropActivityTime = 22
  private const val DropEndTime = 24 + 3 - DropActivityTime
  override var jobKey: JobKey? = null

  class ActivityNotifyJob : Job {
    override fun execute(context: JobExecutionContext?) {
      val jp = ActivityUtil.fetchJPActivity()
      val en = ActivityUtil.fetchENActivity()
      val cn = ActivityUtil.fetchCNActivity()
      val alertListJP = mutableListOf<Activity>()
      val alertListEN = mutableListOf<Activity>()
      val alertListCN = mutableListOf<Activity>()
      val filterJP = jp.first
        .filter {
          filterActive(it, alertListJP)
        } to jp.second
        .filter {
          filterPending(it)
        }.also { insertAlert(alertListJP, ServerLocale.JP) }
      val filterEN = en.first
        .filter {
          filterActive(it, alertListEN)
        } to en.second
        .filter {
          filterPending(it)
        }.also { insertAlert(alertListEN, ServerLocale.GLOBAL) }
      val filterCN = cn.first
        .filter {
          filterActive(it, alertListCN)
        } to cn.second
        .filter {
          filterPending(it)
        }.also { insertAlert(alertListCN, ServerLocale.CN) }
      // 初始化不显示信息
      val init = context?.mergedJobDataMap?.getBoolean(ActivityNotifyDataInitKey) ?: false
      if (init) return
      if (AronaNotifyConfig.enableEveryDay) {
        if (AronaNotifyConfig.enableJP) {
          val jpMessage = ActivityUtil.createActivityImage(filterJP)
          sendMessage(jpMessage, AronaNotifyConfig.notifyStringJP, AronaNotifyConfig.enableJPGroup)
        }
        if (AronaNotifyConfig.enableEN) {
          val enMessage = ActivityUtil.createActivityImage(filterEN, ServerLocale.GLOBAL)
          sendMessage(enMessage, AronaNotifyConfig.notifyStringEN, AronaNotifyConfig.enableENGroup)
        }
        if (AronaNotifyConfig.enableCN) {
          val enMessage = ActivityUtil.createActivityImage(filterCN, ServerLocale.CN)
          sendMessage(enMessage, AronaNotifyConfig.notifyStringCN, AronaNotifyConfig.enableCNGroup)
        }
      }
    }

    private fun sendMessage(imageFile: File, source: String, targetGroup: List<Long>) {
      Arona.sendFilterGroupMessageWithFile(targetGroup) { group ->
        val image = group.uploadImage(imageFile, "png")
        MessageUtil.deserializeMiraiCodeAndBuild(source, group) {
          it.add("\n")
          it.add(image)
          it.build()
        }
      }
    }

    private fun doInsert(activity: List<Activity>, h: Int, locale: ServerLocale, extraKey: String = "") {
      val now = Calendar.getInstance()
      now.set(Calendar.HOUR_OF_DAY, h)
      now.set(Calendar.MINUTE, 0)
      now.set(Calendar.MILLISECOND, 0)
      QuartzProvider.createSingleTask(
        ActivityNotifyOneHourJob::class.java,
        now.time,
        "${ActivityNotifyOneHour}-${locale.commandName}-${h}-${extraKey}",
        ActivityNotifyOneHour,
        mapOf(
          ActivityKey to activity, NotifyStringKey to when (locale) {
            ServerLocale.JP -> AronaNotifyConfig.notifyStringJP
            ServerLocale.GLOBAL -> AronaNotifyConfig.notifyStringEN
            ServerLocale.CN -> AronaNotifyConfig.notifyStringCN
          }
        )
      )
    }

    private fun insertAlert(activity: MutableList<Activity>, locale: ServerLocale) {
      if (activity.isEmpty()) return
      val instance = Calendar.getInstance()
      val dropActivities = activity.filter { isMidnightEndActivity(it) }
      activity.removeAll(dropActivities)
      // 非双倍掉落提醒
      val nowH = instance.get(Calendar.HOUR_OF_DAY)
      if (activity.isNotEmpty()) {
        activity.groupBy { calcDiffDayAndHour(it.time).second }.forEach { (h, u) ->
          doInsert(u, nowH + h - 1, locale, h.toString())
        }
      }
      // 双倍掉落提醒
      if (dropActivities.isNotEmpty()) {
        doInsert(dropActivities, DropActivityTime, locale)
      }
    }

    private fun filterPending(activity: Activity): Boolean {
      val extra = calcDiffDayAndHour(activity.time)
      val d = extra.first
      val h = extra.second
      return doFilter(d, h)
    }

    private fun filterActive(activity: Activity, list: MutableList<Activity>): Boolean {
      val extra = calcDiffDayAndHour(activity.time)
      val d = extra.first
      val h = extra.second
      if (d == 0) {
        list.add(activity)
      }
      return doFilter(d, h)
    }

    private fun doFilter(d: Int, h: Int): Boolean = when (AronaNotifyConfig.notifyType) {
      NotifyType.ALL -> true
      NotifyType.ONLY_24H -> d * 24 + h < 24
      NotifyType.ONLY_48H -> d * 24 + h < 48
    }

  }

  @Suppress("UNCHECKED_CAST")
  class ActivityNotifyOneHourJob : InterruptableJob {
    override fun execute(context: JobExecutionContext?) {
      val ac = context?.mergedJobDataMap?.get(ActivityKey) ?: return
      ac as List<Activity>
      if (ac.isEmpty()) return
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
      val server = ac[0].serverLocale
      val targetGroup = when (server) {
        ServerLocale.JP -> AronaNotifyConfig.enableJPGroup
        ServerLocale.GLOBAL -> AronaNotifyConfig.enableENGroup
        ServerLocale.CN -> AronaNotifyConfig.enableCNGroup
      }
      val serverName = server.serverName
      if (maintenance != null) {
        Arona.sendFilterGroupMessage("距离${serverName}维护还有1小时", targetGroup)
      }
      val notifyPrefix = context.mergedJobDataMap?.get(NotifyStringKey) ?: "${serverName}防侠提醒"
      // 只有维护信息时
      if (activity.isEmpty()) return
      val activityString = activity
        .map { at -> "${at.content}\n" }
        .reduceOrNull { prv, cur -> prv + cur }
      val serverString = if (context.mergedJobDataMap?.get(NotifyStringKey) != null) {
        MiraiCode.deserializeMiraiCode(notifyPrefix as String)
      } else notifyPrefix
      val endTime = if (isMidnightEndActivity(activity[0])) {
        DropEndTime
      } else {
        1
      }
      Arona.sendFilterGroupMessage(
        "${serverString}\n" +
          "$activityString" +
          "将会在${endTime}小时后结束", targetGroup
      )
    }

    override fun interrupt() {
      Arona.warning("interrupt")
    }
  }

  // 判断是不是双倍掉落(一般在3点结束,其实总力战也是,所以放一起了)
  private fun isMidnightEndActivity(activity: Activity): Boolean =
    activity.type in (ActivityType.N2_3..ActivityType.JOINT_EXERCISES)

  private fun isMaintenanceActivity(activity: Activity): Boolean = activity.type == ActivityType.MAINTENANCE

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
      QuartzProvider.triggerTaskWithData(jobKey!!, mapOf(ActivityNotifyDataInitKey to true))
    }
  }

  override val id: Int = 12
  override val name: String = "活动推送"
  override var enable: Boolean = true
}

// 每日防侠提醒类型
enum class NotifyType {
  ALL, ONLY_24H, ONLY_48H
}
