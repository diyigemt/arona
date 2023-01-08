package net.diyigemt.arona.advance

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.quartz.QuartzProvider
import net.diyigemt.arona.service.AronaQuartzService
import net.diyigemt.arona.util.NetworkUtil
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.console.util.SemVersion
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobKey

object AronaUpdateChecker: AronaQuartzService {
  private const val AronaUpdateCheckJobKey = "AronaUpdateCheck"
  override var jobKey: JobKey? = null
  override val id: Int = 14
  override val name: String = "自动更新检查"
  override val description: String = name
  override var isGlobal: Boolean = true

  @kotlinx.serialization.Serializable
  data class VersionInfo(
    val version: String,
    val newFuture: List<String>
  )

  class UpdateCheckJob: Job {
    override fun execute(context: JobExecutionContext?) {
      NetworkUtil.fetchDataFromServerV1<VersionInfo>("/version")
        .onSuccess { response ->
          val version = response.data.version
          val nowVersion = SemVersion(version.replace("v", ""))
          val newFuture = response.data.newFuture
            .mapIndexed { index, element ->
              "${index + 1}. $element"
            }.joinToString("\n")
          val concat = "检测到版本更新,当前版本:${Arona.version}, 新版本:${nowVersion}\n更新日志:\n${newFuture}"
          // 如果本机使用测试版
          if (Arona.version.identifier != null) {
            // 新的版本发布就提醒更新
            if (Arona.version.major == nowVersion.major
              && Arona.version.minor == nowVersion.minor
              && Arona.version.patch == nowVersion.patch
              && Arona.version.identifier != nowVersion.identifier
            ) {
              Arona.sendMessageToAdmin(concat)
            }
          } else {
            if (nowVersion.identifier != null || Arona.version >= nowVersion) return // 忽略测试版和低版本
            Arona.sendMessageToAdmin(concat)
          }
        }
    }
  }

  override fun enableService() {
    //TODO
//    jobKey = QuartzProvider.createCronTask(
//      UpdateCheckJob::class.java,
//      "0 0 ${AronaConfig.updateCheckTime} * * ? *",
//      AronaUpdateCheckJobKey,
//      AronaUpdateCheckJobKey
//    ).first
//    QuartzProvider.createSimpleDelayJob(20) {
//      QuartzProvider.triggerTask(jobKey!!)
//    }
  }
}