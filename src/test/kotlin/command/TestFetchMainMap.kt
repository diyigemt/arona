package org.example.mirai.plugin.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.diyigemt.arona.entity.StageMapHelperTable
import net.diyigemt.arona.util.ImageUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.junit.jupiter.api.Test
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO

const val baseUrl = "https://bluearchive.wikiru.jp/?"
const val baseImageUrl = "https://bluearchive.wikiru.jp/"
const val ChapterName = "%E7%AB%A0"
const val tableFlag = "rgn_content"

class TestFetchMainMap {

  @Test
  fun testFetchStruct() {
    System.setProperty("proxyHost", "127.0.0.1")
    System.setProperty("proxyPort", "7890")
    val chapter = "16-2"
    val final = generateMap(16, chapter) ?: return
    ImageIO.write(final, "png", File("./debug-sandbox/map-cache/${chapter}.png"))
  }

  @Test
  fun fetchAllMap() {
    System.setProperty("proxyHost", "127.0.0.1")
    System.setProperty("proxyPort", "7890")
    (2 .. 19).forEach {
      generateSubChapterList(it).forEach { chapter ->
        val final = generateMap(it, chapter) ?: return
        ImageIO.write(final, "png", File("./debug-sandbox/map-cache/${chapter}.png"))
      }
    }
  }

  private fun generateMap(chapter: Int, chapterName: String): BufferedImage? {
    val body = Jsoup
      .connect("${baseUrl}${chapter}${ChapterName}/${chapterName}")
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .get()
      .body()
    var testIndex = 1
    val container = body.getElementById("${tableFlag}${testIndex}").let {
      return@let if (it == null || it.getElementsByClass("style_table").size == 0) {
        body.getElementById("${tableFlag}${++testIndex}") ?: return null
      } else {
        it
      }
    }
    val stepTable = container.getElementsByClass("style_table").let {
      return@let if (it.size == 0) {
        body.getElementById("${tableFlag}${++testIndex}")!!.getElementsByClass("style_table")[0]
      } else {
        it[0]
      }
    }
    val img = container.getElementsByTag("img").let {
      // 攻略太简单了, 没有配图或者有多余配图
      return@let if (
        it.size == 0 ||
        it[0].attr("title").isBlank() ||
        !it[0].attr("title").contains(chapterName) ||
        it[0].attr("alt").isBlank() ||
        !it[0].attr("alt").contains(chapterName)
      ) {
        val ddd = body.getElementsByTag("a").filter { a ->
          val name = a.attr("title")
          return@filter name.isNotBlank() && name.contains(chapterName) && a.getElementsByTag("img").isNotEmpty()
        }
        ddd[0].getElementsByTag("img")[0]
      } else {
        it[0]
      }
    }
    val imgUrl = "${baseImageUrl}${img.attr("data-src")}"
    println(imgUrl)
    val rows = stepTable.getElementsByTag("tr")
    val tableHeader = mutableListOf("回合")
    val tableContent = mutableListOf<List<StageMapHelperTable.TableCell>>()
    fun colspan(el: Element) = el.attr("colspan").let { s ->
      return@let if (s.isBlank()) 1 else s.toInt()
    }
    // 有多种解法
    val header: Elements
    val trueCol: Int
    if (colspan(rows[0].getElementsByTag("th")[0]) != 1) {
      trueCol = rows.removeAt(0).getElementsByTag("th")[0].attr("colspan").toInt()
      header = rows.removeAt(0).getElementsByTag("th")
    } else {
      header = rows.removeAt(0).getElementsByTag("th")
      trueCol = header.size
    }
    val headerPrefix = header.size > 2
    (1 until trueCol).forEach {
      val source = header[it].text()
      val type = source.substringAfter("部隊")
      val pos = source.substringBefore("部隊")
        .replace("中央", "中")
        .let { s ->
        return@let if (headerPrefix) {
          if (s.contains("ボス")) s.replace("ボス", "路Boss ") else "${s}路 "
        } else ""
      }
      tableHeader.add(
        pos + type
          .replace("戦闘なし", "无战斗")
          .replace("戦闘無し", "无战斗")
          .replace("戦闘", "战斗")
          .replace("配置不要", "无需配置")
      )
    }
    var rowIndex = 1
    rows.forEach { row ->
      val td = row.getElementsByTag("td")
      val content = mutableListOf(
        StageMapHelperTable.TableCell("第${rowIndex++}步")
      )
      td.removeFirst()
      td.forEach {
        val colspan = colspan(it)
        var c = it.text()
        if (c.contains("と位置")) {
          val subIndex = c.indexOf("と位置") - 1
          c = c
            .replace("と位置変更してからワープ", "交换位置后传送")
            .replace("と位置変更してから", "交换位置后向")
            .replace("と位置交換して", "交换位置后向")
            .replace("と位置変更して", "交换位置后向")
            .replace("と位置変更後に", "交换位置后向")
            .replace("と位置変更後", "交换位置后")
          val a = c.substring(0, subIndex - 1)
          val b = c.substring(subIndex)
          c = "${a}与${b}"
        }
        c = c
          .replace("戦闘なし", "无战斗")
          .replace("戦闘無し", "无战斗")
          .replace("戦闘", "战斗")
          .replace("を", "向")
          .replace("は上スタートへ", "回到上出发点")
          .replace("は下スタートへ", "回到下出发点")
          .replace("は左スタートへ", "回到左出发点")
          .replace("は右スタートへ", "回到右出发点")
          .replace("下スタートへ", "回到下出发点")
          .replace("自タイルクリックしてワープ", "点击自己脚底进行传送")
          .replace("ワープ後に", "传送后向")
          .replace("ワープして", "传送至")
          .replace("ワープしない", "不传送")
          .replace("ワープはしない", "不传送")
          .replace("ワープ", "传送")
          .replace("配置不要", "无需配置")
          .replace("行動しない", "不动")
          .replace("動かない", "不动")
          .replace("待機", "不动")
          .replace("は", "向")
        content.add(StageMapHelperTable.TableCell(c, colspan))
      }
      tableContent.add(content)
    }
    val table = StageMapHelperTable(tableContent.size, tableHeader.size, tableHeader, tableContent)
    return ImageUtil.createStageMapHelper(chapterName, imgUrl, table)
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