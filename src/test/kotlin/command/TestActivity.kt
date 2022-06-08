package org.example.mirai.plugin.command

import kotlinx.serialization.json.*
import net.diyigemt.arona.command.ActivityCommand
import net.diyigemt.arona.command.entity.Activity
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class TestActivity {

  val N3H3Plus = Regex("([3双2翻])倍")
  val NormalTime = Regex("[Nn]ormal.*?(\\d+)[月/](\\d+).*?([–-]|~).*?(\\d+)[月/](\\d+)")
  val HardTime = Regex("[Hh]ard.*?(\\d)[月/](\\d+).*?([–-]|~).*?(\\d)[月/](\\d+)")
  @Test
  fun testB() {
    val fetchActivities = fetchActivities()
    val active = mutableListOf<Activity>()
    val pending = mutableListOf<Activity>()
    fetchActivities.forEach {
      val content = it["orig_text"].toString().replace("\\r", "").replace("\\n", "")
      // N3H3
      if (content.contains(Regex("[Nn]ormal")) and content.contains(Regex("[Hh]ard"))) {
        val matchEntire = N3H3Plus.find(content)
        if (matchEntire != null && matchEntire.groups[0] != null) {
          val plus = matchEntire.groups[0]!!.value
          var power = 2
          if (plus.contains("3")) {
            power = 3
          }
          val nTime = NormalTime.find(content)
          val hTime = HardTime.find(content)
          if ((nTime != null) and (hTime != null) and (nTime!!.groups.size == 6) and (hTime!!.groups.size == 6)) {
            val groupN = nTime.groups
            val groupH = hTime.groups
            val n1 = groupN[1]!!.value
            val n2 = groupN[2]!!.value
            val n3 = groupN[4]!!.value
            val n4 = groupN[5]!!.value
            val h1 = groupH[1]!!.value
            val h2 = groupH[2]!!.value
            val h3 = groupH[4]!!.value
            val h4 = groupH[5]!!.value
            val year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
            val startN = "${year}/${if (n1.toInt() < 10) "0$n1" else n1}/${if (n2.toInt() < 10) "0$n2" else n2}"
            val endN = "${year}/${if (n3.toInt() < 10) "0$n3" else n3}/${if (n4.toInt() < 10) "0$n4" else n4}"
            val startH = "${year}/${if (h1.toInt() < 10) "0$h1" else h1}/${if (h2.toInt() < 10) "0$h2" else h2}"
            val endH = "${year}/${if (h3.toInt() < 10) "0$h3" else h3}/${if (h4.toInt() < 10) "0$h4" else h4}"
            val parseStartN = SimpleDateFormat("yyyy/MM/dd").parse(startN)
            val parseEndN = SimpleDateFormat("yyyy/MM/dd").parse(endN)
            val parseStartH = SimpleDateFormat("yyyy/MM/dd").parse(startH)
            val parseEndH = SimpleDateFormat("yyyy/MM/dd").parse(endH)
            val titleN = "N${power}(${startN} - ${endN})"
            val titleH = "H${power}(${startH} - ${endH})"
            val now = Calendar.getInstance().time
            if (now.before(parseStartN)) {
              pending.add(Activity(titleN, power, ""))
            } else if (now.before(parseEndN)) {
              active.add(Activity(titleN, power, ""))
            }
            if (now.before(parseStartH)) {
              pending.add(Activity(titleH, power, ""))
            } else if (now.before(parseEndH)) {
              active.add(Activity(titleH, power, ""))
            }
          }
        }
      }
    }
    val activeString = active.map { at -> "${at.content}     ${at.time}\n" }.reduceOrNull { prv, cur -> prv + cur }
    val pendingString = pending.map { at -> "${at.content}     ${at.time}\n" }.reduceOrNull { prv, cur -> prv + cur }
    println(activeString)
    println(pendingString)
  }

  private fun fetchActivities(): List<JsonObject> {
    var offset = ""
    val res = mutableListOf<JsonObject>()
    for (i in 0 .. 0) {
      val document = Jsoup.connect("https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/space?offset=${offset}&host_mid=1585224247&timezone_offset=-480")
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
        .ignoreContentType(true)
        .get()
      val data = Json.parseToJsonElement(document.text()).jsonObject
      if (clearExtraQute((data["code"] ?: "-1").toString()).toInt() != 0 || clearExtraQute((data["message"] ?: "-1").toString()).toInt() != 0) {
        println("catch BA en activities error, ${data["message"].toString()}")
        continue
      }
      val items = forceGets(data, "data.items").jsonArray
      offset = clearExtraQute(forceGets(data, "data.offset").toString())
      items.forEach {
        val pubType = forceGets(it, "modules.module_author.pub_action")
        if (pubType.toString().length > 2) return@forEach
        val desc = forceGets(it, "modules.module_dynamic.desc.rich_text_nodes").jsonArray
        res.add(desc[0].jsonObject)
      }
    }
    return res
  }

  private fun forceGet(target: JsonElement, key: String): JsonElement {
    return target.jsonObject[key]!!
  }

  private fun forceGets(target: JsonElement, key: String): JsonElement {
    val keys = key.split(".")
    var result: JsonElement = target
    keys.forEach {
      result = result.jsonObject
      result = (result as JsonObject)[it]!!
    }
    return result
  }

  private fun clearExtraQute(s: String): String {
    if (s.replace("\"", "").length + 2 == s.length) {
      return s.replaceFirst("\"", "").substring(0, s.length - 2)
    }
    return s
  }

}