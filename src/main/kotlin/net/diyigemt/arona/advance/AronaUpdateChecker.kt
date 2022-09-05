package net.diyigemt.arona.advance

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.quartz.QuartzProvider
import net.diyigemt.arona.service.AronaQuartzService
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.GeneralUtils.clearExtraQute
import net.diyigemt.arona.util.NetworkUtil
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.console.util.SemVersion
import org.jsoup.Jsoup
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobKey

object AronaUpdateChecker: AronaQuartzService {
  private const val AronaUpdateCheckJobKey = "AronaUpdateCheck"
  override lateinit var jobKey: JobKey
  override val id: Int = 14
  override val name: String = "自动更新检查"
  override var enable: Boolean = true

  override fun init() {
    registerService()
  }

  @kotlinx.serialization.Serializable
  data class VersionInfo(
    val version: String,
    val newFuture: List<String>
  )

  class UpdateCheckJob: Job {
    override fun execute(context: JobExecutionContext?) {
      // 向服务器注册自己
//      if (AronaConfig.uuid.isBlank()) {
//        NetworkUtil.register()
//      }
      val response = NetworkUtil.fetchDataFromServer<VersionInfo>("/version")
      val version = response.data.version
      val nowVersion = SemVersion(version.replace("v", ""))
      if (Arona.version == nowVersion) return
      val newFuture = response.data.newFuture
        .mapIndexed { index, element ->
          "${index + 1}. $element}"
        }.joinToString("\n")
      val concat = "检测到版本更新,当前版本:${Arona.version}, 新版本:${nowVersion}\n更新日志:\n${newFuture}"
      Arona.sendMessageToAdmin(concat)
    }
  }

  override fun enableService() {
    jobKey = QuartzProvider.createCronTask(
      UpdateCheckJob::class.java,
      "0 0 ${AronaConfig.updateCheckTime} * * ? *",
      AronaUpdateCheckJobKey,
      AronaUpdateCheckJobKey
    ).first
    QuartzProvider.createSimpleDelayJob(20) {
      QuartzProvider.triggerTask(jobKey)
    }
  }
}