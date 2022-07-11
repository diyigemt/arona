package net.diyigemt.arona.advance

import kotlinx.coroutines.runBlocking
import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.NGAPushConfig
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.quartz.QuartzProvider
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import okhttp3.*
import org.jsoup.Jsoup
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.io.InputStream
import java.util.*

object NGAImageTranslatePusher: InitializedFunction() {
  private const val ImageTranslateCheckJobKey = "ImageTranslateCheck"
  private const val ImageSrcBaseAddress = "https://img.nga.178.com/attachments/"
  private val cookies = mutableMapOf(
    "ngaPassportUid" to "",
    "ngaPassportCid" to "",
    "lastvisit" to "",
    "lastpath" to ""
  )
  private val users = mapOf(
    "42382305" to "xiwang399",
    "40785736" to "安kuzuha"
  )
  private const val ImageRegex = "\\[img][.]/([\\w/-]+[.]jpg)\\[/img]"
  private var lastFloorTime: String = ""
  override fun init() {
    if (!NGAPushConfig.enable) return
    if (NGAPushConfig.cid == "" && NGAPushConfig.uid == "") {
      Arona.warning("nga推送配置未初始化,请修改nga.yml配置文件")
      return
    }
    val key = QuartzProvider.createRepeatSingleTask(
      TranslatePusherJob::class.java,
      NGAPushConfig.checkInterval,
      ImageTranslateCheckJobKey,
      ImageTranslateCheckJobKey
    )
    cookies["ngaPassportUid"] = NGAPushConfig.uid
    cookies["ngaPassportCid"] = NGAPushConfig.cid
    QuartzProvider.createSimpleDelayJob(20) {
      QuartzProvider.triggerTask(key.first)
    }
  }

  class TranslatePusherJob: Job {
    override fun execute(context: JobExecutionContext?) {
      Arona.warning("hello")
      val fetchNGA = fetchNGA().also {
        if (it.isEmpty()) {
          Arona.warning("arona获取nga图楼信息失败,请检查配置文件是否有误")
          return
        }
      }
      val pending = fetchNGA.filter {
        it.time > lastFloorTime
      }.also {
        if (it.isEmpty()) return
      }
      pending.map { floor ->
        Arona.sendMessageWithFile {
          val builder = MessageChainBuilder()
          val userName = users[floor.uid]
          builder.add("$userName\n")
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

  private data class NGAFloor(
    val uid: String,
    val time: String,
    val images: List<String>
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
      val user = it.getElementsByClass("posterinfo")[0]?.getElementsByTag("a")?.get(0)?.attr("href")?.substringAfter("uid=") ?: return@forEach
      val time = it.getElementsByClass("postInfo")[0]?.getElementsByTag("span")?.text() ?: return@forEach
      val content = it.getElementsByClass("postcontent")[0]?.text() ?: return@forEach
      if (!users.containsKey(user)) return@forEach
      val reg = Regex(ImageRegex)
      val findAll = reg.findAll(content)
      val imgSrc = mutableListOf<String>()
      if (findAll.toList().isEmpty()) return@forEach
      findAll
        .filter {
            mr -> mr.groups.size >= 2
        }
        .map {
            mr -> mr.groupValues[1]
        }.forEach {
            mr -> imgSrc.add(mr)
        }
      res.add(NGAFloor(user, time, imgSrc))
    }
    return res
  }

  private fun fetchImageFromNGA(href: String): InputStream? {
    val builder = OkHttpClient.Builder()
    builder.cookieJar(CookieJarImp())
    val client = builder.build()
    val request = Request.Builder()
      .url("${ImageSrcBaseAddress}${href}")
      .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .get()
      .build()
    val response = client.newCall(request).execute()
    if (response.isSuccessful) {
      return response.body?.byteStream()
    }
    return null
  }

  private class CookieJarImp: CookieJar {
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


}