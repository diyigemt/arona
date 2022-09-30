package net.diyigemt.arona.util

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.entity.ImageResult
import net.diyigemt.arona.entity.ServerResponse
import net.diyigemt.arona.util.sys.SysStatic
import net.mamoe.mirai.console.plugin.version
import org.jsoup.Connection
import org.jsoup.Connection.Response
import org.jsoup.Jsoup
import java.io.File
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

object NetworkUtil {
//  const val BACKEND_ADDRESS = "https://arona.diyigemt.com"
  const val BACKEND_ADDRESS = "http://localhost:12201"
  private const val BACKEND_API_ADDRESS = "$BACKEND_ADDRESS/api/v1"
  private const val BACKEND_IMAGE_API = "/image"
  private const val AUTH_HEADER = "Authorization"
  private const val VERSION_HEADER = "version"

  // 向后端服务器注册自己
  fun registerInstance() {
    val sysSave = SysDataUtil.get(SysStatic.UUID)
    if (sysSave == null) {
      safeNetworkWithoutId(NetworkUtil::registerInstance0)
        .onSuccess {
          val header = it.header(AUTH_HEADER)
          if (header.isNullOrBlank()) {
            Arona.warning("register failure")
            return
          }
          SysDataUtil.saveRegisterData(header)
          AronaConfig.uuid = header
        }.onFailure {
          Arona.warning("register failure")
        }
    } else {
      AronaConfig.uuid = sysSave
    }
  }

  private fun registerInstance0(): Response {
    return sendDataToServerSource("/user/register")
  }

  private fun logoutInstance0(): Response {
    return sendDataToServerSource("/user/offline")
  }

  fun logoutInstance() {
    safeNetwork(NetworkUtil::logoutInstance0)
  }

  private fun safeNetworkWithoutId(func: KFunction<Response>): Result<Response> {
    func.isAccessible = true
    return kotlin.runCatching {
      func.call()
    }.onFailure { it.printStackTrace() }
  }

  private fun safeNetwork(func: KFunction<Response>): Result<Response> {
    if (AronaConfig.uuid.isEmpty()) {
      return kotlin.runCatching { throw Exception() }
    }
    return safeNetworkWithoutId(func)
  }

  fun requestImage(name: String): ServerResponse<List<ImageResult>> =
    fetchDataFromServer(BACKEND_IMAGE_API, mapOf("name" to name))

  inline fun <reified T> fetchDataFromServer(
    api: String,
    data: Map<String, String> = mutableMapOf()
  ): ServerResponse<T> {
    val response = fetchDataFromServerSource(api, data)
    return Json.decodeFromString(response.body())
  }

  fun fetchDataFromServerSource(api: String, data: Map<String, String> = mutableMapOf()): Response {
    return baseRequest(api)
      .data(data)
      .execute()
  }

  private inline fun <reified T> sendDataToServer(
    api: String,
    data: Map<String, String> = mutableMapOf()
  ): ServerResponse<T> {
    val response = sendDataToServerSource(api, data)
    return Json.decodeFromString(response.body())
  }

  private fun sendDataToServerSource(api: String, data: Map<String, String> = mutableMapOf()): Response {
    return baseRequest(api)
      .data(data)
      .method(Connection.Method.POST)
      .execute()
  }

  fun baseRequest(api: String, source: String = BACKEND_API_ADDRESS): Connection =
    Jsoup.connect("$source${api}")
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .header(AUTH_HEADER, AronaConfig.uuid)
      .header(VERSION_HEADER, Arona.version.toString())
      .maxBodySize(1024 * 1024 * 10)
}