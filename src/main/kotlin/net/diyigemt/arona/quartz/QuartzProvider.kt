package net.diyigemt.arona.quartz

import kotlinx.coroutines.Dispatchers
import net.diyigemt.arona.interfaces.BaseFunctionProvider
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import java.util.*

object QuartzProvider: BaseFunctionProvider(Dispatchers.IO) {

  private lateinit var quartzScheduler: Scheduler

  override val tag: String = "quartz"

  fun createSingleTask(jobClass: Class<out Job>, expected: Date, jobKey: String, jobGroup: String, jogData: Map<String, Any>? = null): Pair<JobKey, TriggerKey> {
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



  override suspend fun main() {
    quartzScheduler = StdSchedulerFactory.getDefaultScheduler().also { it.start() }
  }

  override fun disable() {
    quartzScheduler.shutdown()
  }

}