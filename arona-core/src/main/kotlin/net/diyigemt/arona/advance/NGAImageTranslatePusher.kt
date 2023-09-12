package net.diyigemt.arona.advance

import net.diyigemt.arona.Arona
import net.diyigemt.arona.Arona.save
import net.diyigemt.arona.config.NGAPushConfig
import net.diyigemt.arona.quartz.QuartzProvider
import net.diyigemt.arona.service.AronaQuartzService
import net.diyigemt.arona.util.NetworkUtil
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.jsoup.Jsoup
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import java.io.InputStream
import java.util.*

object NGAImageTranslatePusher : AronaQuartzService {
  private const val ImageTranslateCheckJobKey = "ImageTranslateCheck"
  private const val ImageSrcBaseAddress = "https://img.nga.178.com/attachments/"
  private const val ImageTranslateCheckInitKey = "init"
  private val cookies = mutableMapOf(
    "ngaPassportUid" to "",
    "ngaPassportCid" to "",
    "lastvisit" to "",
    "lastpath" to ""
  )
  private const val ImageRegex =
    "\\[img][.]/([\\w/-]+)[.](jpg|png|JPG|PNG)([.](medium|large|small|thumb|thumb_s|thumb_ss)[.](jpg|png|JPG|PNG))?\\[/img]"
  private const val maxCache: Int = 3
  override var jobKey: JobKey? = null
  override val id: Int = 13
  override val name: String = "nga图楼推送"
  override var enable: Boolean = true

  class TranslatePusherJob : Job {
    override fun execute(context: JobExecutionContext?) {
      val fetchNGA = fetchNGA().let {
        if (it == null) {
          Arona.warning("arona获取nga图楼信息失败,请检查配置文件是否有误")
          return
        }
        it
      }
      val cache = NGAPushConfig.cache
      val isNew = cache.isEmpty()
      val pending = fetchNGA.filter {
        !cache.any { c -> c.second == it.postId }
      }.also {
        if (it.isEmpty()) {
          Arona.info("arona获取nga图楼信息: 新图列表为空")
          return
        }
      }
      updateCache(pending)
      val init = context?.mergedJobDataMap?.getBooleanValue(ImageTranslateCheckInitKey) ?: false
      if (init && isNew) {
        return
      }
      pending.map { floor ->
        val imageExternalResource = floor.images.mapNotNull {
          fetchImageFromNGA(it)?.use { stream ->
            stream.readAllBytes()
          }
        }
        val userName = NGAPushConfig.watch[floor.uid]
        Arona.sendMessageWithFile(NGAPushConfig.sendInterval) { group ->
          // 过滤应该发送的uid
          val filterUid = NGAPushConfig.groupUidFilterMap[group.id]
          if (filterUid != null && filterUid.contains(floor.uid)) {
            return@sendMessageWithFile null
          }
          if (NGAPushConfig.forwardThreshold != 0 && NGAPushConfig.forwardThreshold <= floor.images.size) {
            val builder = ForwardMessageBuilder(group)
            builder.add(Arona.arona!!, PlainText("$userName(${floor.uid}):\n${floor.content}\n"))
            imageExternalResource.forEach { bytes ->
              bytes.toExternalResource().use { resource ->
                val image = group.uploadImage(resource)
                builder.add(Arona.arona!!, image)
              }
            }
            builder.build()
          } else {
            val builder = MessageChainBuilder()
            builder.add("$userName(${floor.uid}):\n")
            builder.add("${floor.content}\n")
            imageExternalResource.forEach { bytes ->
              bytes.toExternalResource().use { resource ->
                val image = group.uploadImage(resource)
                builder.add(image)
              }
            }
            builder.build()
          }
        }
        // 降低发送频率
        Thread.sleep(2000)
      }
    }
  }

  private fun updateCache(now: List<NGAFloor>) {
    val nowDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    NGAPushConfig.cache.removeIf { nowDay - it.first >= maxCache }
    now.forEach {
      NGAPushConfig.cache.add(nowDay to it.postId)
    }
    NGAPushConfig.save()
  }

  private data class NGAFloor(
    val uid: String,
    val time: String,
    val content: String,
    val images: List<String>,
    val postId: String
  )

  private fun fetchNGA(): List<NGAFloor>? {
    val random = Calendar.getInstance().time.time - Random().nextInt(25) + 5
    cookies["lastvisit"] = random.toString()
    cookies["lastpath"] = "/read.php?tid=${cookies["ngaPassportUid"]}"
    val body = NetworkUtil.request(
      Jsoup.connect("https://${NGAPushConfig.source.url}/read.php?tid=30843163")
    ).cookies(cookies)
      .get()
      .body()
    val mainContent = body.getElementsByClass("forumbox")
    if (mainContent.size < 2) return null
    val res = mutableListOf<NGAFloor>()
    mainContent.removeAt(0)
    mainContent.forEach { it ->
      val user =
        it.getElementsByClass("posterinfo")[0]?.getElementsByTag("a")?.get(0)?.attr("href")?.substringAfter("uid=")
          ?: return@forEach
      if (!NGAPushConfig.watch.containsKey(user)) return@forEach
      val time = it.getElementsByClass("postInfo")[0]?.getElementsByTag("span")?.text() ?: return@forEach
      val content = it.getElementsByClass("postcontent")[0]?.text() ?: return@forEach
      val reg = Regex(ImageRegex)
      val findAll = reg.findAll(content).toList().also { all -> if (all.isEmpty()) return@forEach }
      val imgSrc = mutableListOf<String>()
      // 有图才爬内容
      val content0 = content.substringBefore("[")
      findAll
        .filter { mr ->
          mr.groups.size >= 2
        }
        .map { mr ->
          "${mr.groupValues[1]}.${mr.groupValues[2]}"
        }.forEach { mr ->
          imgSrc.add(mr)
        }
      val postIdArr = it.getElementsByTag("a")
        .map { a -> a.attr("id") }
        .filter { s -> s.contains("Anchor") && s.contains("pid") }
        .also {
          if (it.isEmpty()) return@forEach
        }
      val postId = postIdArr[0].replace("pid", "").replace("Anchor", "")
      res.add(NGAFloor(user, time, content0, imgSrc, postId))
    }
    return res
  }

  private fun fetchImageFromNGA(href: String): InputStream? {
    return NetworkUtil.request(
      Jsoup
        .connect("${ImageSrcBaseAddress}${href}")
        .cookies(cookies)
    ).response().bodyStream()
  }

  enum class NGASource(val url: String) {
    MAIN("ngabbs.com"), SUB("nga.178.com")
  }

  override fun init() {
    registerService()
  }

  override fun enableService() {
    if (NGAPushConfig.cid == "" && NGAPushConfig.uid == "") {
      Arona.warning("nga推送配置未初始化,请修改nga.yml配置文件")
      return
    }
    cookies["ngaPassportUid"] = NGAPushConfig.uid
    cookies["ngaPassportCid"] = NGAPushConfig.cid
    jobKey = QuartzProvider.createRepeatSingleTask(
      TranslatePusherJob::class.java,
      NGAPushConfig.checkInterval,
      ImageTranslateCheckJobKey,
      ImageTranslateCheckJobKey
    ).first
    QuartzProvider.createSimpleDelayJob(20) {
      QuartzProvider.triggerTaskWithData(jobKey!!, mapOf(ImageTranslateCheckInitKey to true))
    }
  }
}
