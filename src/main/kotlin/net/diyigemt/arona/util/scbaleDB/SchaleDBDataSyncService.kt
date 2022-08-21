package net.diyigemt.arona.util.scbaleDB

import net.diyigemt.arona.service.AronaQuartzService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobKey

/**
 *@Author hjn
 *@Create 2022/8/20
 */
object SchaleDBDataSyncService : AronaQuartzService{
  override lateinit var jobKey: JobKey
  override val id: Int = 19
  override val name: String = "数据同步服务"
  override var enable: Boolean = true

  class SchaleDBDataSyncJob : Job{
    override fun execute(context: JobExecutionContext?) {
      TODO("Not yet implemented")
    }
  }

  override fun init() {
    registerService()
  }

  // TODO start cron task
  override fun enableService() {
    super.enableService()
  }

  // TODO cancel cron task
  override fun disableService() {
    super.disableService()
  }
}