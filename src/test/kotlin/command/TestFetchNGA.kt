package org.example.mirai.plugin.command

import okhttp3.*
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileOutputStream
import java.util.*

object TestFetchNGA {

  val imageSrcBaseAddr = "https://img.nga.178.com/attachments/"
  val tid = "42164110"
  val cid = "Z8ghnljttk4qes3j9tg9bl4c0rampo98ecpdjdsl"
  val cookies = mutableMapOf(
    "ngaPassportUid" to tid,
    "ngaPassportCid" to cid,
    "lastvisit" to "",
    "lastpath" to ""
  )
  val targets = mapOf("42382305" to "xiwang399", "40785736" to "安kuzuha")
  val reg = "\\[img][.]/([\\w/-]+[.]jpg)\\[/img]"
  @Test
  fun testFetchNGA() {
    prepare()
    val res = Jsoup.connect("https://ngabbs.com/read.php?tid=30843163")
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .cookies(cookies)
      .get()
      .body()
    val mainContent = res.getElementsByClass("forumbox")
    if (mainContent.size < 2) return
    mainContent.removeAt(0)
    val result = mutableListOf<Pair<String, String>>()
    mainContent.forEach {
      val user = it.getElementsByClass("posterinfo").get(0)?.getElementsByTag("a")?.get(0)?.attr("href")?.substringAfter("uid=") ?: return@forEach
      val time = it.getElementsByClass("postInfo").get(0)?.getElementsByTag("span")?.text() ?: return@forEach
      val content = it.getElementsByClass("postcontent").get(0)?.text() ?: return@forEach
      if (!targets.containsKey(user)) return@forEach
      val reg = Regex(reg)
      val findAll = reg.findAll(content)
      if (findAll.toList().isEmpty()) return@forEach
      val s = findAll
        .filter {
          mr -> mr.groups.size >= 2
        }
        .map {
        mr -> mr.groupValues[1]
      }.joinToString("\n")
      result.add(time to s)
    }
    println("success")
  }

  @Test
  fun testFetchUser() {
    prepare()
    val res = Jsoup.connect("https://ngabbs.com/nuke.php?func=ucp&uid=60864974")
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .cookies(cookies)
      .get()
      .body()
    println(res)
  }

  @Test
  fun testDownloadImageFromNGA() {
//    val res = Jsoup.connect("${imageSrcBaseAddr}mon_202207/11/n6Q2q-6bc8K2qT3cSsg-k4.jpg")
//      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
//      .ignoreContentType(true)
//      .cookies(cookies)
//      .get()
//      .body()
//    println(res)
    fetchImg("${imageSrcBaseAddr}mon_202207/11/n6Q2q-6bc8K2qT3cSsg-k4.jpg")
  }

  fun fetchImg(url: String) {
    val builder = OkHttpClient.Builder()
    builder.cookieJar(CookieJarImp())
    val client = builder.build()
    val request = Request.Builder()
      .url(url)
      .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .get()
      .build()
    val response = client.newCall(request).execute()
    if (response.isSuccessful) {
      val stream = response.body?.byteStream() ?: return
      val fileOutputStream = FileOutputStream(File("test.jpg"))
      fileOutputStream.write(stream.readAllBytes())
    }
  }

  class CookieJarImp: CookieJar {
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
      prepare()
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

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
      println("save")
    }
  }

  fun prepare() {
    val random = Calendar.getInstance().time.time - Random().nextInt(25) + 5
    cookies["lastvisit"] = random.toString()
    cookies["lastpath"] = "/read.php?tid=$tid"
  }

}