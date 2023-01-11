package net.diyigemt.arona.advance

import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.announcement.RemoteActionModel
import net.diyigemt.arona.db.announcement.RemoteActionTable
import net.diyigemt.arona.interfaces.ConfigReader
import net.diyigemt.arona.interfaces.getConfig
import net.diyigemt.arona.quartz.QuartzProvider
import net.diyigemt.arona.remote.RemoteServiceManager
import net.diyigemt.arona.service.AronaQuartzService
import net.diyigemt.arona.util.NetworkUtil
import org.jetbrains.exposed.sql.SortOrder
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobKey

object AronaRemoteActionChecker : AronaQuartzService, ConfigReader {
  private const val AronaRemoteActionCheckJobKey = "AronaAnnouncementCheck"
  override var jobKey: JobKey? = null
  override val id: Int = 23
  override val name: String = "远端服务"
  override val description: String = name
  override var isGlobal: Boolean = true
  override val configPrefix: String = "remote"

  class RemoteActionCheckJob : Job {
    override fun execute(context: JobExecutionContext?) {
      // 获取本地已读的最近10条操作
      val read = DataBaseProvider.query {
        RemoteActionModel.all().limit(10).orderBy(RemoteActionTable.id to SortOrder.DESC).map { it.aid }
      }!!
      NetworkUtil.fetchDataFromServerV1<List<RemoteActionItem>>(
        "/action", mutableMapOf(
          "read" to read.joinToString(",")
        )
      ).onSuccess {
        RemoteServiceManager.dispatchService(it.data)
      }.onFailure {
        it.printStackTrace()
      }
    }
  }

  override fun enableService() {
    val interval = getConfig<Int>("checkInterval")
    if (interval == 0) {
      return
    }
    jobKey = QuartzProvider.createRepeatSingleTask(
      RemoteActionCheckJob::class.java,
      interval * 60,
      AronaRemoteActionCheckJobKey,
      AronaRemoteActionCheckJobKey
    ).first
    QuartzProvider.createSimpleDelayJob(20) {
      QuartzProvider.triggerTask(jobKey!!)
    }
  }

}

@kotlinx.serialization.Serializable
data class RemoteActionItem(
  val id: Long,
  val action: String,
  val content: String,
  val time: String
)