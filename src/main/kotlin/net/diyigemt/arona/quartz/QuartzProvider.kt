package net.diyigemt.arona.quartz

import kotlinx.coroutines.Dispatchers
import net.diyigemt.arona.Arona
import net.diyigemt.arona.interfaces.BaseFunctionProvider
import net.mamoe.mirai.console.util.safeCast
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

object QuartzProvider: BaseFunctionProvider(Dispatchers.IO) {

  private val quartzScheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler().also { it.start() }
  private const val SimpleDelaySendMessageJobKey: String = "SimpleDelaySendMessageJobKey"
  private const val SimpleDelaySendMessageJobData: String = "SimpleDelaySendMessageJobData"
  private const val SimpleDelaySendMessageJobTarget: String = "SimpleDelaySendMessageJobTarget"
  override val tag: String = "quartz"

  fun createSingleTask(jobClass: Class<out Job>, expected: Date, jobKey: String, jobGroup: String, jogData: Map<String, Any>? = mapOf()): Pair<JobKey, TriggerKey> {
    val jobKeys = JobKey.jobKey("${jobKey}Job", jobGroup)
    val triggerKey = TriggerKey.triggerKey("${jobKey}Trigger", jobGroup)
    val job = JobBuilder
      .newJob(jobClass)
      .withIdentity(jobKeys)
      .setJobData(JobDataMap(jogData))
      .build()
    val trigger = TriggerBuilder
      .newTrigger()
      .withIdentity(triggerKey)
      .withSchedule(
        SimpleScheduleBuilder
          .simpleSchedule()
          .withRepeatCount(0)
          .withMisfireHandlingInstructionFireNow()
          .withIntervalInSeconds(1)
      )
      .startAt(expected)
      .build()
    quartzScheduler.scheduleJob(job, trigger)
    return jobKeys to triggerKey
  }

  fun createCronTask(jobClass: Class<out Job>, expected: String, jobKey: String, jobGroup: String, jogData: Map<String, Any> = mapOf()): Pair<JobKey, TriggerKey> {
    val jobKeys = JobKey.jobKey("${jobKey}Job", jobGroup)
    val triggerKey = TriggerKey.triggerKey("${jobKey}Trigger", jobGroup)
    val job = JobBuilder
      .newJob(jobClass)
      .withIdentity(jobKeys)
      .setJobData(JobDataMap(jogData))
      .build()
    val trigger = TriggerBuilder
      .newTrigger()
      .withIdentity(triggerKey)
      .withSchedule(
        CronScheduleBuilder.cronSchedule(expected)
      )
      .startNow()
      .build()
    quartzScheduler.scheduleJob(job, trigger)
    return jobKeys to triggerKey
  }

  private fun createSimpleDelaySendMessageJob(expected: Date, type: MessageTarget, message: String): Pair<JobKey, TriggerKey> {
    val dataMap = mapOf(
      SimpleDelaySendMessageJobTarget to type,
      SimpleDelaySendMessageJobData to message
    )
    return createSingleTask(
      SimpleDelaySendMessageJob::class.java,
      expected,
      SimpleDelaySendMessageJobKey,
      SimpleDelaySendMessageJobKey,
      dataMap
    )
  }

  fun createSimpleDelaySendMessageJob(delay: Long, type: MessageTarget, message: String): Pair<JobKey, TriggerKey> {
    val now = Calendar.getInstance()
    now.set(Calendar.SECOND, (now.get(Calendar.SECOND) + delay).toInt())
    Arona.info(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now.time))
    return createSimpleDelaySendMessageJob(
      now.time,
      type,
      message
    )
  }

  enum class MessageTarget {
    MANAGER_GROUP, SERVICE_GROUP
  }

  private class SimpleDelaySendMessageJob: Job {
    override fun execute(context: JobExecutionContext?) {
      val message = context?.mergedJobDataMap?.getString(SimpleDelaySendMessageJobData) ?: return
      val target = context.mergedJobDataMap?.get(SimpleDelaySendMessageJobTarget) ?: return
      when(target.safeCast<MessageTarget>()) {
        MessageTarget.MANAGER_GROUP -> Arona.sendMessageToAdmin(message)
        MessageTarget.SERVICE_GROUP -> Arona.sendMessage(message)
        null -> {}
      }
    }
  }

  fun triggerTaskWithData(jobKey: String, group: String, data: Map<String, Any>) = quartzScheduler.triggerJob(JobKey.jobKey("${jobKey}Job", group), JobDataMap(data))

  fun triggerTask(jobKey: String, group: String) = quartzScheduler.triggerJob(JobKey.jobKey("${jobKey}Job", group))

  fun deleteTask(jobKey: String, group: String): Boolean = deleteTask(JobKey.jobKey("${jobKey}Job", group))

  fun interruptTask(jobKey: String, group: String): Boolean = interruptTask(JobKey.jobKey("${jobKey}Job", group))

  fun pauseTask(jobKey: String, group: String) = pauseTask(JobKey.jobKey("${jobKey}Job", group))

  fun resumeTask(jobKey: String, group: String) = resumeTask(JobKey.jobKey("${jobKey}Job", group))

  fun deleteTask(jobKey: JobKey): Boolean = quartzScheduler.deleteJob(jobKey)

  fun interruptTask(jobKey: JobKey): Boolean = quartzScheduler.interrupt(jobKey)

  fun pauseTask(jobKey: JobKey) = quartzScheduler.pauseJob(jobKey)

  fun resumeTask(jobKey: JobKey) = quartzScheduler.resumeJob(jobKey)

  override suspend fun main() {}

  override fun disable() {
    quartzScheduler.shutdown()
  }

}