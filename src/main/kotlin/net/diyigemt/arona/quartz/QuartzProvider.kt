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
  private const val SimpleDelayJobKey: String = "SimpleDelayJobKey"
  private const val SimpleDelayJobData: String = "SimpleDelayJobData"
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

  fun createRepeatSingleTask(jobClass: Class<out Job>, interval: Int, jobKey: String, jobGroup: String, jogData: Map<String, Any>? = mapOf()): Pair<JobKey, TriggerKey> {
    val jobKeys = JobKey.jobKey("${jobKey}Job", jobGroup)
    val triggerKey = TriggerKey.triggerKey("${jobKey}Trigger", jobGroup)
    val now = Calendar.getInstance()
    now.get(Calendar.MINUTE).also {
      if (it > 30) {
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) + 1)
      } else {
        now.set(Calendar.MINUTE, 30)
      }
    }
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
          .repeatForever()
          .withMisfireHandlingInstructionFireNow()
          .withIntervalInMinutes(interval)
      )
      .startAt(now.time)
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

  fun createSimpleDelayJob(delay: Int, block: () -> Unit): Pair<JobKey, TriggerKey> {
    val now = Calendar.getInstance()
    now.set(Calendar.SECOND, now.get(Calendar.SECOND) + delay)
    return createSingleTask(
      SimpleDelayJob::class.java,
      now.time,
      "$SimpleDelayJobKey${now.time.time}",
      SimpleDelayJobKey,
      mapOf(
        SimpleDelayJobData to block
      )
    )
  }

  @Suppress("UNCHECKED_CAST")
  class SimpleDelayJob: Job {
    override fun execute(context: JobExecutionContext?) {
      val block = context?.mergedJobDataMap?.get(SimpleDelayJobData) ?: return
      (block as () -> Unit)()
    }
  }

  fun triggerTaskWithData(jobKey: JobKey, data: Map<String, Any>) = quartzScheduler.triggerJob(jobKey, JobDataMap(data))

  fun triggerTaskWithData(jobKey: String, group: String, data: Map<String, Any>) = quartzScheduler.triggerJob(JobKey.jobKey("${jobKey}Job", group), JobDataMap(data))

  fun triggerTask(jobKey: String, group: String) = quartzScheduler.triggerJob(JobKey.jobKey("${jobKey}Job", group))

  fun triggerTask(jobKey: JobKey) = quartzScheduler.triggerJob(jobKey)

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