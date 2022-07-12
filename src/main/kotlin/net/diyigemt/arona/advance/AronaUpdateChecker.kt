package net.diyigemt.arona.advance

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.quartz.QuartzProvider
import net.diyigemt.arona.util.GeneralUtils.clearExtraQute
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.console.util.SemVersion
import org.jsoup.Jsoup
import org.quartz.Job
import org.quartz.JobExecutionContext

object AronaUpdateChecker: InitializedFunction() {
  private const val AronaUpdateCheckJobKey = "AronaUpdateCheck"
  override fun init() {
    if (!AronaConfig.autoCheckUpdate) return
    val keys = QuartzProvider.createCronTask(
      UpdateCheckJob::class.java,
      "0 0 ${AronaConfig.updateCheckTime} * * ? *",
      AronaUpdateCheckJobKey,
      AronaUpdateCheckJobKey
    )
    QuartzProvider.createSimpleDelayJob(20) {
      QuartzProvider.triggerTask(keys.first)
    }
  }

  class UpdateCheckJob: Job {
    override fun execute(context: JobExecutionContext?) {
      val get = Jsoup.connect("http://localhost:3000/api/v1/version/")
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
        .ignoreContentType(true)
        .get()
      val text = get.body().text()
      val parseToJsonElement = Json.parseToJsonElement(text)
      val jsonObject = parseToJsonElement.jsonObject["data"]?.jsonObject ?: return
      val version = jsonObject["version"].toString().replace("\"", "")
      val nowVersion = SemVersion(version)
      if (Arona.version == nowVersion) return
      val newFuture = jsonObject["newFuture"]?.jsonArray
        ?.mapIndexed { index, element ->
          "${index + 1}.${clearExtraQute(element.toString())}"
        }?.joinToString("\n")
        ?: return
      val concat = "检测到版本更新,当前版本:${Arona.version}, 新版本:${nowVersion}\n更新日志:\n${newFuture}"
      Arona.sendMessageToAdmin(concat)
    }
  }
}