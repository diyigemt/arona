package net.diyigemt.arona.advance

import kotlinx.coroutines.runBlocking
import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.NGAPushConfig
import net.diyigemt.arona.quartz.QuartzProvider
import net.diyigemt.arona.service.AronaQuartzService
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.select.NodeFilter
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
  private const val ImageRegex = "\\[img][.]/([\\w/-]+[.](jpg|png|JPG|PNG))\\[/img]"
  private var lastFloorTime: String = ""
  private const val maxCache: Int = 5
  override lateinit var jobKey: JobKey
  override val id: Int = 13
  override val name: String = "nga图楼推送"
  override var enable: Boolean = true

  class TranslatePusherJob : Job {
    override fun execute(context: JobExecutionContext?) {
      val fetchNGA = fetchNGA().also {
        if (it.isEmpty()) {
          Arona.warning("arona获取nga图楼信息失败,请检查配置文件是否有误")
          return
        }
      }
      val cache = NGAPushConfig.cache
      val pending = fetchNGA.filter {
        it.time > lastFloorTime || !cache.contains(it.postId)
      }.also {
        if (it.isEmpty()) return
      }
      val init = context?.mergedJobDataMap?.getBooleanValue(ImageTranslateCheckInitKey) ?: false
      updateCache(cache, pending)
      if (init) {
        lastFloorTime = pending[0].time
        return
      }
      pending.map { floor ->
        Arona.sendMessageWithFile {
          val builder = MessageChainBuilder()
          val userName = NGAPushConfig.watch[floor.uid]
          builder.add("$userName(${floor.uid}):\n")
          builder.add("${floor.content}\n")
          runBlocking {
            floor.images.forEach { href ->
              val i = fetchImageFromNGA(href)?.toExternalResource() ?: return@forEach
              val image = it.uploadImage(i)
              builder.add(image)
              i.close()
            }
          }
          builder.build()
        }
        // 降低发送频率
        Thread.sleep(2000)
      }
      lastFloorTime = pending[0].time
    }
  }

  private fun updateCache(cache: MutableList<String>, now: List<NGAFloor>) {
    if (cache.size >= maxCache) {
      cache.clear()
    }
    now.forEach {
      cache.add(it.postId)
    }
  }

  private data class NGAFloor(
    val uid: String,
    val time: String,
    val content: String,
    val images: List<String>,
    val postId: String
  )

  private fun fetchNGA(): List<NGAFloor> {
    val res = mutableListOf<NGAFloor>()
    val random = Calendar.getInstance().time.time - Random().nextInt(25) + 5
    cookies["lastvisit"] = random.toString()
    cookies["lastpath"] = "/read.php?tid=${cookies["ngaPassportUid"]}"
    val body = Jsoup.connect("https://ngabbs.com/read.php?tid=30843163")
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .cookies(cookies)
      .get()
      .body()
    val mainContent = body.getElementsByClass("forumbox")
    if (mainContent.size < 2) return res
    mainContent.removeAt(0)
    mainContent.forEach {
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
          mr.groupValues[1]
        }.forEach { mr ->
          imgSrc.add(mr)
        }
      val postId = it.getElementsByTag("a").filter { node, _ -> return@filter if (node.attr("id").contains("Anchor")) NodeFilter.FilterResult.CONTINUE else NodeFilter.FilterResult.SKIP_ENTIRELY }
      res.add(NGAFloor(user, time, content0, imgSrc, postId[0]?.attr("id") ?: ""))
    }
    return res
  }

  private fun fetchImageFromNGA(href: String): InputStream? {
    val builder = OkHttpClient.Builder()
    builder.cookieJar(CookieJarImp())
    val client = builder.build()
    val request = Request.Builder()
      .url("${ImageSrcBaseAddress}${href}")
      .addHeader(
        "user-agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36"
      )
      .get()
      .build()
    val response = client.newCall(request).execute()
    if (response.isSuccessful) {
      return response.body?.byteStream()
    }
    return null
  }

  private class CookieJarImp : CookieJar {
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
      return cookies.entries.map {
        val builder = Cookie.Builder()
        return@map builder
          .name(it.key)
          .value(it.value)
          .domain("ngabbs.com")
          .path("/")
          .build()
      }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {}
  }

  override fun init() {
    registerService()
  }

  override fun enableService() {
    if (NGAPushConfig.cid == "" && NGAPushConfig.uid == "") {
      Arona.warning("nga推送配置未初始化,请修改nga.yml配置文件")
      return
    }
    jobKey = QuartzProvider.createRepeatSingleTask(
      TranslatePusherJob::class.java,
      NGAPushConfig.checkInterval,
      ImageTranslateCheckJobKey,
      ImageTranslateCheckJobKey
    ).first
    cookies["ngaPassportUid"] = NGAPushConfig.uid
    cookies["ngaPassportCid"] = NGAPushConfig.cid
    QuartzProvider.createSimpleDelayJob(20) {
      QuartzProvider.triggerTaskWithData(jobKey, mapOf(ImageTranslateCheckInitKey to true))
    }
  }
}