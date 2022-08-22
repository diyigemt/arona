package org.example.mirai.plugin.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.diyigemt.arona.entity.StageMapHelperTable
import net.diyigemt.arona.util.ImageUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream

const val baseUrl = "https://bluearchive.wikiru.jp/?"
const val baseImageUrl = "https://bluearchive.wikiru.jp/"
const val chapterName = "%E7%AB%A0"
const val tableFlag = "rgn_content"

class TestFetchMainMap {

  @Test
  fun testFetchStruct() {
    System.setProperty("proxyHost", "127.0.0.1")
    System.setProperty("proxyPort", "7890")
    val body = Jsoup
      .connect("${baseUrl}15${chapterName}/15-3")
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .get()
      .body()
    var testIndex = 1
    val container = body.getElementById("${tableFlag}${testIndex}") ?: return
    val stepTable = container.getElementsByClass("style_table").let {
      return@let if (it.size == 0) {
        body.getElementById("${tableFlag}${++testIndex}")!!.getElementsByClass("style_table")[0]
      } else {
        it[0]
      }
    }
    val img = container.getElementsByTag("img")[0] ?: return
    val imgUrl = "${baseImageUrl}${img.attr("data-src")}"
    println(imgUrl)
    val rows = stepTable.getElementsByTag("tr")
    val tableHeader = mutableListOf("回合")
    val tableContent = mutableListOf<List<StageMapHelperTable.TableCell>>()
    val header = rows[0].getElementsByTag("th")
    header.removeAt(0)
    header.forEach { tableHeader.add(it.text().substringAfter("部隊")) }
    rows.removeAt(0)
    var rowIndex = 1
    rows.forEach { row ->
      val td = row.getElementsByTag("td")
      val content = mutableListOf(
        StageMapHelperTable.TableCell("第${rowIndex++}步")
      )
      td.removeAt(0)
      td.forEach {
        val colspan = it.attr("colspan").let { s->
          return@let if (s.isBlank()) 1 else s.toInt()
        }
        var c = it.text()
          .replace("を", "向")
          .replace("は上スタートへ", "先向上")
          .replace("は下スタートへ", "先向下")
        if (c.contains("と位置")) {
          val subIndex = c.indexOf("と位置") - 1
          c = c
            .replace("と位置交換して", "交换位置后向")
            .replace("と位置変更して", "交换位置后向")
          val a = c.substring(0, subIndex - 1)
          val b = c.substring(subIndex)
          c = "${a}与${b}"
        }
        content.add(StageMapHelperTable.TableCell(c.replace("(", "\n("), colspan))
      }
      tableContent.add(content)
    }
    val table = StageMapHelperTable(rows.size + 1, header.size + 1, tableHeader, tableContent)
    ImageUtil.createStageMapHelper(imgUrl, table)
  }

  private fun fetchImg(url: String) {
    val builder = OkHttpClient.Builder()
    builder.cookieJar(TestFetchNGA.CookieJarImp())
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

  private fun generateSubChapterList(chapter: Int): List<String> {
    return listOf(
      "${chapter}-1",
      "${chapter}-2",
      "${chapter}-3",
      "${chapter}-4",
      "${chapter}-5",
      "H${chapter}-1",
      "H${chapter}-2",
      "H${chapter}-3",
      )
  }

}