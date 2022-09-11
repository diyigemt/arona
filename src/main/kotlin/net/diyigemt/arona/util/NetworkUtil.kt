package net.diyigemt.arona.util

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.entity.ImageResult
import net.diyigemt.arona.entity.ServerResponse
import net.mamoe.mirai.console.plugin.version
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File

object NetworkUtil {
  const val BACKEND_ADDRESS = "https://arona.diyigemt.net"
//  const val BACKEND_ADDRESS = "http://localhost:12201"
  private const val BACKEND_API_ADDRESS = "$BACKEND_ADDRESS/api/v1"
  private const val BACKEND_IMAGE_API = "/image"
  private const val AUTH_HEADER = "Authorization"
  private const val VERSION_HEADER = "version"
  fun register() {
    val resp = sendDataToServerSource("/user/register")
    val header = resp.header(AUTH_HEADER)
    if (header.isNullOrBlank()) return
    AronaConfig.uuid = header
  }

  fun requestImage(name: String): ServerResponse<ImageResult> = fetchDataFromServer(BACKEND_IMAGE_API, mapOf("name" to name))

  inline fun <reified T> fetchDataFromServer(api: String, data: Map<String, String> = mutableMapOf()): ServerResponse<T> {
    val response = fetchDataFromServerSource(api, data)
    return Json.decodeFromString(response.body())
  }

  fun fetchDataFromServerSource(api: String, data: Map<String, String> = mutableMapOf()): Connection.Response {
    return baseRequest(api)
      .data(data)
      .execute()
  }

  private inline fun <reified T> sendDataToServer(api: String, data: Map<String, String> = mutableMapOf()): ServerResponse<T> {
    val response = sendDataToServerSource(api, data)
    return Json.decodeFromString(response.body())
  }

  private fun sendDataToServerSource(api: String, data: Map<String, String> = mutableMapOf()): Connection.Response {
    return baseRequest(api)
      .data(data)
      .method(Connection.Method.POST)
      .execute()
  }

  fun baseRequest(api: String, source: String = BACKEND_API_ADDRESS): Connection = Jsoup.connect("$source${api}")
    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
    .ignoreContentType(true)
    .header(AUTH_HEADER, AronaConfig.uuid)
    .header(VERSION_HEADER, Arona.version.toString())
    .maxBodySize(1024 * 1024 * 10)
}