package org.example.mirai.plugin

import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ActivityType
import net.diyigemt.arona.util.ActivityUtil
import net.diyigemt.arona.util.WikiruUtil
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.imageio.ImageIO

class TestSimple {

  @Test
  fun testParseDate() {
    val parse = SimpleDateFormat("yyyy/M/d HH").parse("2022/12/12 12")
    println(parse)
  }

  @Test
  fun testDateAdd() {
    val instance = Calendar.getInstance()
    instance.set(Calendar.DAY_OF_MONTH, 28)
    instance.set(Calendar.MONTH, 5)
    instance.set(Calendar.DAY_OF_MONTH, instance.get(Calendar.DAY_OF_MONTH) + 6)
    println(instance.get(Calendar.DAY_OF_MONTH))
  }

  @Test
  fun testParseInt() {
    try {
        "123asd".toInt()
    } catch (_: Exception) {
      println(123)
    }
  }

//  @Test
//  fun testGetVersion() {
//    val get = Jsoup.connect("http://localhost:3000/api/v1/version/")
//      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
//      .ignoreContentType(true)
//      .get()
//    val parseToJsonElement = Json.parseToJsonElement(get.body().text())
//    val jsonElement = parseToJsonElement.jsonObject["data"]?.jsonObject?.get("version")
//    println(jsonElement.toString().replace("\"", ""))
//  }
//
//  @Test
//  fun testEmptyArrayGet() {
//    val arr = mutableListOf<String>()
//    println(arr[0] ?: return)
//  }

  @Test
  fun testEnumClass() {
    println(A.B in (A.C .. A.E))
  }

  enum class A {
    A, B, C, D, E
  }

  fun testA(){
    val cmd = "diff"
    val page = "イベント一覧"
    val res = Jsoup.connect("https://bluearchive.wikiru.jp?cmd=${cmd}&page=${page}")
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .get()
      .body()
      .getElementById("body")
      ?.getElementsByTag("pre")!!
      res.select(".diff_removed").remove()

    val str = WikiruUtil.getValidData(res.text())
    print(WikiruUtil.analyze(str))
    print("\n")
  }

  @Test
  fun testDrawImage() {
    System.setProperty("java.awt.headless", "true")
    val active = listOf(
      Activity("PickUp 春阿鲁 春月", "05天11小时后结束", ActivityType.PICK_UP),
      Activity("总力战[球·室内战]", "05天11小时后结束", ActivityType.DECISIVE_BATTLE),
    )
    val pending = listOf(
      Activity("特殊作战3倍掉落", "05天11小时后开始", ActivityType.SPECIAL_DROP)
    )
    val img = ActivityUtil.createActivityImage(active to pending)
    ImageIO.write(img, "png", File("test.png"))
  }

}