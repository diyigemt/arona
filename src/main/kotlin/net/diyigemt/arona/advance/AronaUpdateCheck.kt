package net.diyigemt.arona.advance

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.quartz.QuartzProvider
import org.jsoup.Jsoup
import org.quartz.Job
import org.quartz.JobExecutionContext

object AronaUpdateCheck: InitializedFunction() {
  private const val AronaUpdateCheckJobKey = "AronaUpdateCheck"
  override fun init() {
    QuartzProvider.createCronTask(
      UpdateCheckJob::class.java,
      "0 0 ${AronaConfig.updateCheckTime} * * ? *",
      AronaUpdateCheckJobKey,
      AronaUpdateCheckJobKey
    )
    QuartzProvider.triggerTask(AronaUpdateCheckJobKey, AronaUpdateCheckJobKey)
  }

  class UpdateCheckJob: Job {
    override fun execute(context: JobExecutionContext?) {
      val get = Jsoup.connect("http://localhost:3000/api/v1/version/")
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
        .ignoreContentType(true)
        .get()
      val parseToJsonElement = Json.parseToJsonElement(get.body().text())
      val jsonElement = parseToJsonElement.jsonObject["data"]?.jsonObject?.get("version") ?: {

      }
      println(jsonElement.toString().replace("\"", ""))
    }
  }
}