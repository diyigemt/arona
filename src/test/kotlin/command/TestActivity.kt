package org.example.mirai.plugin.command

import kotlinx.serialization.json.*
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test

class TestActivity {

  @Test
  fun testB() {
    val document = Jsoup.connect("https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/space?offset=&host_mid=1585224247&timezone_offset=-480")
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .get()
    val data = Json.parseToJsonElement(document.text()).jsonObject
    if (clearExtraQute((data["code"] ?: "-1").toString()).toInt() != 0 || clearExtraQute((data["message"] ?: "-1").toString()).toInt() != 0) {
      println("catch error")
      println(data["message"].toString())
      return
    }
    val items = forceGets(data, "data.items").jsonArray
    items.forEach {
      val pubType = forceGets(it, "modules.module_author.pub_action")
      if (pubType.toString().length > 2) return@forEach
      val desc = forceGets(it, "modules.module_dynamic.desc.rich_text_nodes").jsonArray
      println(desc[0].jsonObject["orig_text"])
    }
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