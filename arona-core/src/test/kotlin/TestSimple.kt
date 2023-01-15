package org.example.mirai.plugin

import io.kotest.common.runBlocking
import kotlinx.coroutines.channels.consumeEach
import net.diyigemt.arona.command.TrainerCommand
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ActivityType
import net.diyigemt.arona.remote.action.VersionInfo
import net.diyigemt.arona.util.ActivityUtil
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.NetworkUtil
import net.diyigemt.arona.util.WikiruUtil
import net.diyigemt.arona.util.other.KWatchChannel
import net.diyigemt.arona.util.other.KWatchEvent
import net.diyigemt.arona.util.other.asWatchChannel
import net.diyigemt.arona.util.scbaleDB.SchaleDBDataSyncService
import net.diyigemt.arona.util.scbaleDB.SchaleDBUtil
import net.mamoe.mirai.console.util.SemVersion
import net.mamoe.yamlkt.Yaml
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class TestSimple {

  @Test
  fun testParseDate() {
    val parse = SimpleDateFormat("yyyy/M/d HH").parse("2022/12/12 12")
    println(parse)
  }

  @Test
  fun testVersion() {
    println(SemVersion("1.0.1-M1"))
    println(SemVersion("1.0.1-M1").identifier)
    println(SemVersion("1.0.1-M1") >= SemVersion("1.0.1"))
  }

  @Test
  fun testDateAdd() {
    val instance = Calendar.getInstance()
    instance.set(Calendar.DAY_OF_MONTH, 28)
    instance.set(Calendar.MONTH, 5)
    instance.set(Calendar.DAY_OF_MONTH, instance.get(Calendar.DAY_OF_MONTH) + 6)
    println(instance.get(Calendar.DAY_OF_MONTH))
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
    println(A.B in (A.C..A.E))
  }

  enum class A {
    A, B, C, D, E
  }

  fun testA() {
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
  }

  @Test
  fun testSchaleDB() {
    System.setProperty("proxyHost", "127.0.0.1")
    System.setProperty("proxyPort", "7890")
    SchaleDBDataSyncService.SchaleDBDataSyncJob().getData()
    val res = SchaleDBUtil.getJPEventData()
    print(res)
  }

  @Test
  fun testRandom() {
    val times = 10
    val sigma = (1..5).map {
      val record = FloatArray(22) { 0f }
      (1..times).forEach { _ ->
        record[GeneralUtils.randomInt(22)]++
      }
      val avg = record.sum() / 22
      val sigma = record.sumOf { (it - avg).pow(2).toDouble() }
      (sigma / times).pow(0.5)
    }
    val avg = sigma.sum() / 5
    val sigma0 = sigma.sumOf { (it - avg).pow(2) }
    println((sigma0 / 5).pow(0.5))
  }

  @Test
  fun testRandom2() {
    var a = 0
    var b = 0
    (1..10).forEach { _ ->
      if (GeneralUtils.randomBoolean()) a++ else b++
      Thread.sleep((1..10).random().toLong())
    }
    println(a)
    println(b)
  }

  @Test
  fun testVersionCheck() {
    val cur = SemVersion("1.0.6")
    NetworkUtil.fetchDataFromServerV1<VersionInfo>("/version")
      .onSuccess { response ->
        val version = response.data.version
        val nowVersion = SemVersion(version.replace("v", ""))
        if (cur == nowVersion) return
        val newFuture = response.data.newFuture
          .mapIndexed { index, element ->
            "${index + 1}. $element"
          }.joinToString("\n")
        val concat = "检测到版本更新,当前版本:${cur}, 新版本:${nowVersion}\n更新日志:\n${newFuture}"
        println(concat)
      }
  }

  @Test
  fun testFileWatcher() {
    val dir = File("D:\\123.txt")
    if (!dir.exists()) {
      dir.writeText("")
    }
    val channel = dir.asWatchChannel(KWatchChannel.Mode.SingleFile)
    runBlocking {
      channel.consumeEach {
        when (it.kind) {
          KWatchEvent.Kind.Modified -> {
            val obj = Yaml.decodeFromString(
              TrainerCommand.TrainerFileConfig.serializer(),
              it.file.readText(Charsets.UTF_8)
            )
            obj.override.forEach { to ->
              println(to.name)
            }
          }

          else -> {}
        }
      }
    }
  }

}