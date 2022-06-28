package org.example.mirai.plugin.quartz

import org.junit.jupiter.api.Test
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import java.util.*
import java.util.Calendar

class TestSingleTimer {

  @Test
  fun testA() {
    val schedule = StdSchedulerFactory.getDefaultScheduler().also { it.start() }
    class TestJob: Job {
      override fun execute(context: JobExecutionContext?) {
        println("hello world!")
      }
    }
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
    var jobDetail = schedule.getJobDetail(res.first)
    println(jobDetail == null)
    Thread.sleep(5 * 1000)
    jobDetail = schedule.getJobDetail(res.first)
    println(jobDetail == null)
    Thread.sleep(5 * 1000)
    jobDetail = schedule.getJobDetail(res.first)
    println(jobDetail == null)
  }


}