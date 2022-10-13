package org.example.mirai.plugin.quartz

import net.diyigemt.arona.quartz.QuartzProvider
import org.junit.jupiter.api.Test
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import java.util.*
import java.util.Calendar

class TestSingleTimer {

  @Test
  fun testA() {
    val schedule = StdSchedulerFactory.getDefaultScheduler().also { it.start() }
    fun createSingleTask(jobClass: Class<out Job>, expected: Date, jobKey: String, jobGroup: String): Pair<JobKey, TriggerKey> {
      val key = JobKey.jobKey("${jobKey}Job", jobGroup)
      val tr = TriggerKey.triggerKey("${jobKey}Trigger", jobGroup)
      val job = JobBuilder
        .newJob(jobClass)
        .withIdentity(key)
        .build()
      val trigger = TriggerBuilder
        .newTrigger()
        .withIdentity(tr)
        .withSchedule(
          SimpleScheduleBuilder
            .simpleSchedule()
            .withRepeatCount(0)
            .withMisfireHandlingInstructionFireNow()
            .withIntervalInSeconds(1)
        )
        .startAt(expected)
        .build()
      schedule.scheduleJob(job, trigger)
      return key to tr
    }

    val now = Calendar.getInstance()
    now.set(Calendar.SECOND, now.get(Calendar.SECOND) + 5)
    val res = createSingleTask(TestJob::class.java, now.time, "name", "group1")
    var jobDetail = schedule.getJobDetail(JobKey.jobKey("nameJob", "group1"))
    println(jobDetail == null)
    Thread.sleep(5 * 1000)
    jobDetail = schedule.getJobDetail(JobKey.jobKey("nameJob", "group1"))
    println(jobDetail == null)
    Thread.sleep(5 * 1000)
    jobDetail = schedule.getJobDetail(JobKey.jobKey("nameJob", "group1"))
    println(jobDetail == null)
  }

  @Test
  fun testTriggerWithData() {
    val schedule = StdSchedulerFactory.getDefaultScheduler().also { it.start() }
    val key = JobKey.jobKey("Job", "group")
    val tr = TriggerKey.triggerKey("Trigger", "group")
    val now = Calendar.getInstance()
    now.set(Calendar.SECOND, now.get(Calendar.SECOND) + 5)
    val job = JobBuilder
      .newJob(TestJob::class.java)
      .withIdentity(key)
      .build()
    val trigger = TriggerBuilder
      .newTrigger()
      .withIdentity(tr)
      .withSchedule(
        SimpleScheduleBuilder
          .simpleSchedule()
          .withRepeatCount(0)
          .withMisfireHandlingInstructionFireNow()
          .withIntervalInSeconds(1)
      )
      .startAt(now.time)
      .build()
    schedule.scheduleJob(job, trigger)
    schedule.triggerJob(key, JobDataMap(mapOf("test" to true)))
    Thread.sleep(6000)
  }

  class TestJob: Job {
    override fun execute(context: JobExecutionContext?) {
      println("hello world!")
      context?.mergedJobDataMap?.entries?.forEach { println(it.value) }
      println(context?.mergedJobDataMap?.getBoolean("test"))
    }
  }

  @Test
  fun testDelayJob() {
    val schedule = StdSchedulerFactory.getDefaultScheduler().also { it.start() }
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
      schedule.scheduleJob(job, trigger)
      return jobKeys to triggerKey
    }
    fun createSimpleDelayJob(delay: Int, block: () -> Unit): Pair<JobKey, TriggerKey> {
      val now = Calendar.getInstance()
      now.set(Calendar.SECOND, now.get(Calendar.SECOND) + delay)
      return createSingleTask(
        SimpleDelayJob::class.java,
        now.time,
        "${123}${now.time.time}",
        "123",
        mapOf(
          "data" to block
        )
      )
    }
    createSimpleDelayJob(2) {
      println("hahaha")
    }
    Thread.sleep(1000 * 5)
  }

  @Suppress("UNCHECKED_CAST")
  class SimpleDelayJob: Job {
    override fun execute(context: JobExecutionContext?) {
      val block = context?.mergedJobDataMap?.get("data") ?: return
      (block as () -> Unit)()
    }
  }



}