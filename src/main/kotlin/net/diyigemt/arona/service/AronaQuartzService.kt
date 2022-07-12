package net.diyigemt.arona.service

import net.diyigemt.arona.quartz.QuartzProvider
import org.quartz.JobKey

interface AronaQuartzService: AronaService {
  var jobKey: JobKey

  override fun disableService() {
    QuartzProvider.deleteTask(jobKey)
  }
}