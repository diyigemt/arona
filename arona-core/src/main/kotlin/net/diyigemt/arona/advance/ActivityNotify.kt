package net.diyigemt.arona.advance

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.GlobalConfigProvider
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ActivityType
import net.diyigemt.arona.entity.ServerLocale
import net.diyigemt.arona.interfaces.*
import net.diyigemt.arona.quartz.QuartzProvider
import net.diyigemt.arona.service.AronaQuartzService
import net.diyigemt.arona.util.ActivityUtil
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import org.quartz.InterruptableJob
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import java.io.File
import java.util.*

object ActivityNotify: AronaQuartzService, ConfigReader {
  private const val ActivityNotifyJobKey = "ActivityNotify"
  private const val ActivityNotifyDataInitKey = "init"
  private const val ActivityNotifyOneHour = "ActivityNotifyOneHour"
  private const val ActivityKey = "activity"
  private const val DropActivityTime = 22
  private const val DropEndTime = 24 + 3 - DropActivityTime
  override var jobKey: JobKey? = null

  class ActivityNotifyJob: Job {
    override fun execute(context: JobExecutionContext?) {
      val jp: Pair<List<Activity>, List<Activity>> = ActivityUtil.fetchJPActivity()
      val en = ActivityUtil.fetchENActivity()
      val alertListJP = jp.first.filter { extraHAndD(it).second == 0 }
      val alertListEN = en.first.filter { extraHAndD(it).second == 0 }
      insertAlert(alertListJP.toMutableList())
      insertAlert(alertListEN.toMutableList())
      // 初始化不显示信息
      val init = context?.mergedJobDataMap?.getBoolean(ActivityNotifyDataInitKey) ?: false
      if (init) return
      val activeGroup = GlobalConfigProvider.getGroupList()
      val enableJP = activeGroup.filter { getGroupConfig("enableJP", it) }
      val enableEN = activeGroup.filter { getGroupConfig("enableEN", it) }
      // 如果有群开启了日服通知
      if (enableJP.isNotEmpty()) {
        val jpMessage = ActivityUtil.createActivityImage(jp)
        sendMessage(jpMessage, enableJP, ServerLocale.JP)
      }
      // 如果有群开启了国际服通知
      if (enableJP.isNotEmpty()) {
        val jpMessage = ActivityUtil.createActivityImage(en)
        sendMessage(jpMessage, enableEN, ServerLocale.GLOBAL)
      }
    }

    private fun sendMessage(imageFile: File, groups: List<Long>, locale: ServerLocale) {
      groups.forEach { group ->
        Arona.sendGroupMessage(group) { contact ->
          val tip = getGroupConfig<String>(if (locale == ServerLocale.JP) "notifyStringJP" else "notifyStringEN", group)
          val image = contact.uploadImage(imageFile, "png")
          MessageUtil.deserializeMiraiCodeAndBuild(tip, contact) { builder ->
            builder.add("\n")
            builder.add(image)
            builder.build()
          }
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
      val nowH = instance.get(Calendar.HOUR_OF_DAY)
      if (activity.isNotEmpty()) {
        activity.groupBy { extraHAndD(it).first }.forEach { (h, u) ->
          doInsert(u, nowH + h - 1, h.toString())
        }
      }
      // 双倍掉落提醒
      if (dropActivities.isNotEmpty()) {
        doInsert(dropActivities, DropActivityTime)
      }
    }

    private fun extraHAndD(activity: Activity): Pair<Int, Int> {
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
      val activity = ac.filter {
        it.type != ActivityType.MAINTENANCE
      }
      // 只有维护信息时
      if (activity.isEmpty()) return
      val activityString = activity
        .map { at -> "${at.content}\n" }
        .reduceOrNull { prv, cur -> prv + cur }
      val endTime = if (isMidnightEndActivity(activity[0])) {
        DropEndTime
      } else {
        1
      }

      val activeGroup = GlobalConfigProvider.getGroupList()
      val serviceGroup = activeGroup.filter { getGroupServiceConfig("活动推送", it) }
      // 根据防侠类型过滤掉打开通知的群
      val configKey = if (server) "enableJP" else "enableEN"
      val notifyConfigKey = if (server) "notifyStringJP" else "notifyStringEN"
      val notifyGroup = serviceGroup.filter { getGroupServiceConfig(configKey, it) }
      notifyGroup.forEach {
        val serverString = getGroupConfig<String>(notifyConfigKey, it)
        Arona.sendGroupMessage(it) {
          this.add("${serverString}\n" +
            "$activityString" +
            "将会在${endTime}小时后结束")
          this.build()
        }
      }
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

  override fun enableService() {
    val time = getMainConfig<Int>("everyDayHour")
    // 每天早上8点触发
    jobKey = QuartzProvider.createCronTask(
      ActivityNotifyJob::class.java,
      "0 0 $time * * ? *",
      ActivityNotifyJobKey,
      ActivityNotifyJobKey
    ).first
    QuartzProvider.createSimpleDelayJob(20) {
      QuartzProvider.triggerTaskWithData(jobKey!!, mapOf(ActivityNotifyDataInitKey to true))
    }
  }

  override val id: Int = 12
  override val name: String = "活动推送"
  override val description: String = name
  override var isGlobal: Boolean = false
  override val configPrefix: String = "notify"
}
