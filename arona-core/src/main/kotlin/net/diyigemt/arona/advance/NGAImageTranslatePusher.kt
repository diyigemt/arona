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
  private const val maxCache: Int = 3
  override var jobKey: JobKey? = null
  override val id: Int = 13
  override val name: String = "nga图楼推送"
  override val description: String = name
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
      val isNew = cache.isEmpty()
      val pending = fetchNGA.filter {
        !cache.any { c -> c.second == it.postId }
      }.also {
        if (it.isEmpty()) return
      }
      updateCache(cache, pending)
      val init = context?.mergedJobDataMap?.getBooleanValue(ImageTranslateCheckInitKey) ?: false
      if (init && isNew) {
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
    }
  }

  private fun updateCache(cache: MutableList<Pair<Int, String>>, now: List<NGAFloor>) {
    val nowDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    cache.removeIf { nowDay - it.first >= maxCache }
    now.forEach {
      cache.add(nowDay to it.postId)
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
    val body = Jsoup.connect("https://${NGAPushConfig.source.url}/read.php?tid=30843163")
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .cookies(cookies)
      .get()
      .body()
    val mainContent = body.getElementsByClass("forumbox")
    if (mainContent.size < 2) return res
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
          mr.groupValues[1]
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
          .domain(NGAPushConfig.source.url)
          .path("/")
          .build()
      }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {}
  }

  enum class NGASource(val url: String) {
    MAIN("ngabbs.com"), SUB("nga.178.com")
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