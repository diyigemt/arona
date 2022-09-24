package net.diyigemt.arona.advance

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.announcement.AnnouncementModel
import net.diyigemt.arona.db.announcement.AnnouncementTable
import net.diyigemt.arona.quartz.QuartzProvider
import net.diyigemt.arona.service.AronaQuartzService
import net.diyigemt.arona.util.NetworkUtil
import org.jetbrains.exposed.sql.SortOrder
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobKey

object AronaAnnouncementChecker : AronaQuartzService {
  private const val AronaAnnouncementCheckJobKey = "AronaAnnouncementCheck"
  override var jobKey: JobKey? = null
  override val id: Int = 23
  override val name: String = "插件公告"
  override var enable: Boolean = true

  override fun init() {
    registerService()
  }

  @kotlinx.serialization.Serializable
  data class AnnouncementItem(
    val id: Long,
    val content: String,
    val time: String
  )

  class AnnouncementCheckJob : Job {
    override fun execute(context: JobExecutionContext?) {
      // 获取本地已读的最近10条消息
      val read = DataBaseProvider.query {
        AnnouncementModel.all().limit(10).orderBy(AnnouncementTable.id to SortOrder.DESC).map { it.aid }
      }!!
      val response = NetworkUtil.fetchDataFromServer<List<AnnouncementItem>>(
        "/announcement", mutableMapOf(
          "read" to read.joinToString(",")
        )
      )
      val announcement = response.data.let {
        it.ifEmpty {
          return
        }
      }.mapIndexed { index, element ->
          DataBaseProvider.query {
            AnnouncementModel.new {
              this.aid = element.id
            }
          }
          "${index + 1}. ${element.content}(${element.time})"
        }.joinToString("\n")
      val concat = "新公告:\n${announcement}"
      Arona.sendMessageToAdmin(concat)
      println("aaa")
    }
  }

  override fun enableService() {
    val interval = AronaConfig.announcementCheckInterval
    if (interval == 0) {
      return
    }
    jobKey = QuartzProvider.createRepeatSingleTask(
      AnnouncementCheckJob::class.java,
      interval * 60,
      AronaAnnouncementCheckJobKey,
      AronaAnnouncementCheckJobKey
    ).first
    QuartzProvider.createSimpleDelayJob(20) {
      QuartzProvider.triggerTask(jobKey!!)
    }
  }
}