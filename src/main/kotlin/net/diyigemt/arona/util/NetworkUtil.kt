package net.diyigemt.arona.util

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.entity.ServerResponse
import net.mamoe.mirai.console.plugin.version
import org.jsoup.Connection
import org.jsoup.Jsoup
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

object NetworkUtil {
  const val BACKEND_ADDRESS = "https://arona.diyigemt.net"
  private const val BACKEND_API_ADDRESS = "$BACKEND_ADDRESS/api/v1"
  inline fun <reified T> fetchDataFromServer(api: String, data: MutableMap<String, String> = mutableMapOf()): ServerResponse<T> {
    val response = fetchDataFromServerSource(api, data)
    return Json.decodeFromString(response.body())
  }

  fun fetchDataFromServerSource(api: String, data: MutableMap<String, String> = mutableMapOf()): Connection.Response {
    return baseRequest(api)
      .data(data)
      .execute()
  }

  inline fun <reified T> sendDataToServer(api: String, data: Any): ServerResponse<T> {
    val response = sendDataToServerSource(api, data)
    return Json.decodeFromString(response.body())
  }

  fun sendDataToServerSource(api: String, data: Any): Connection.Response {
    val map = mutableMapOf<String, String>()
    data::class.memberProperties.forEach {
      it.isAccessible = true
      map[it.name] = it.call(null).toString()
    }
    return baseRequest(api)
      .data(map)
      .method(Connection.Method.POST)
      .execute()
  }

  fun baseRequest(api: String, source: String = BACKEND_API_ADDRESS): Connection = Jsoup.connect("$source${api}")
    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
    .ignoreContentType(true)
    .header("Authorization", AronaConfig.uuid)
    .header("version", Arona.version.toString())

}