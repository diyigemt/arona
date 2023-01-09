package net.diyigemt.arona.util

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.diyigemt.arona.Arona
import net.diyigemt.arona.entity.ImageResult
import net.diyigemt.arona.entity.ServerResponse
import net.diyigemt.arona.event.BaseDatabaseInitEvent
import net.diyigemt.arona.interfaces.ConfigReader
import net.diyigemt.arona.interfaces.Initialize
import net.diyigemt.arona.interfaces.getMainConfig
import net.diyigemt.arona.interfaces.setConfig
import net.diyigemt.arona.util.sys.SysStatic
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.event.globalEventChannel
import org.jsoup.Connection
import org.jsoup.Connection.Response
import org.jsoup.Jsoup
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File

object NetworkUtil: Initialize, ConfigReader {
  const val BACKEND_ADDRESS = "https://arona.diyigemt.com"
  private const val CDN_ADDRESS = "https://arona.cdn.diyigemt.com"
//  const val BACKEND_ADDRESS = "http://localhost:12201"
  const val BACKEND_IMAGE_FOLDER = "/image"
  private const val BACKEND_FILE_FOLDER = "/file"
  private const val BACKEND_FILE_RESOURCE = "${BACKEND_ADDRESS}$BACKEND_FILE_FOLDER"
  private const val AUTH_HEADER = "Authorization"
  private const val VERSION_HEADER = "version"

  // 向后端服务器注册自己
  private fun registerInstance() {
    sendDataToServerSourceV1("/user/register")
      .onSuccess {
        val header = it.header(AUTH_HEADER)
        if (header.isNullOrBlank()) {
          Arona.warning("register failure")
          return
        }
        setConfig("uuid", header)
        Arona.info("register success")
      }.onFailure {
        Arona.warning("register failure")
      }
  }

  fun logoutInstance() {
    sendDataToServerSourceV1("/user/offline")
  }

  fun requestImage(name: String): Result<ServerResponse<List<ImageResult>>> =
    fetchDataFromServerV1(BACKEND_IMAGE_FOLDER, mapOf("name" to name))

  inline fun <reified T> fetchDataFromServerV1(
    api: String,
    data: Map<String, String> = mutableMapOf()
  ): Result<ServerResponse<T>> =
    fetchDataFromServerSourceV1(api, data).map {
      Json.decodeFromString(it.body())
    }
  fun fetchDataFromServerSourceV1(api: String, data: Map<String, String> = mutableMapOf()): Result<Response> =
    kotlin.runCatching {
      baseRequest(api, ApiVersion.V1)
        .data(data)
        .execute()
    }
  private inline fun <reified T> sendDataToServerV1(
    api: String,
    data: Map<String, String> = mutableMapOf()
  ): Result<ServerResponse<T>> = sendDataToServerSourceV1(api, data).map {
    Json.decodeFromString(it.body())
  }
  private fun sendDataToServerSourceV1(api: String, data: Map<String, String> = mutableMapOf()): Result<Response> =
    sendDataToServerSource(api, ApiVersion.V1, data)
  inline fun <reified T> fetchDataFromServer(
    api: String,
    version: ApiVersion,
    data: Map<String, String> = mutableMapOf()
  ): Result<ServerResponse<T>> =
    fetchDataFromServerSource(api, version, data).map {
      Json.decodeFromString(it.body())
    }
  fun fetchDataFromServerSource(api: String, version: ApiVersion, data: Map<String, String> = mutableMapOf()): Result<Response> =
    kotlin.runCatching {
      baseRequest(api, version)
        .data(data)
        .execute()
    }
  private inline fun <reified T> sendDataToServer(
    api: String,
    version: ApiVersion,
    data: Map<String, String> = mutableMapOf()
  ): Result<ServerResponse<T>> = sendDataToServerSource(api, version, data).map {
    Json.decodeFromString(it.body())
  }
  private fun sendDataToServerSource(api: String, version: ApiVersion, data: Map<String, String> = mutableMapOf()): Result<Response> =
    kotlin.runCatching {
      baseRequest(api, version)
        .data(data)
        .method(Connection.Method.POST)
        .execute()
    }

  private fun baseRequest(api: String, version: ApiVersion): Connection =
    request("${version.getAddress()}${api}")

  private fun cdnRequest(api: String, source: String = CDN_ADDRESS): Connection =
    request("$source${api}")


  private fun request(api: String): Connection =
    Jsoup.connect(api)
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .ignoreContentType(true)
      .header(AUTH_HEADER, getMainConfig("uuid")) // 提供身份识别token
      .header(VERSION_HEADER, Arona.version.toString()) // 提供客户端版本号
      .maxBodySize(1024 * 1024 * 100) // 最大下载文件大小100M
      .timeout(30 * 1000) // 30s连接失败抛出异常

  fun downloadImageFile(path: String, localFile: File): Result<File> = downloadFileFromCDN("${BACKEND_IMAGE_FOLDER}$path", localFile)
  fun downloadFileFile(path: String, localFile: File): Result<File> = downloadFileFromCDN("${BACKEND_FILE_FOLDER}$path", localFile)

  private fun downloadFileFromCDN(path: String, localFile: File): Result<File> =
    kotlin.runCatching {
      val conn = cdnRequest(path)
      writeFile(localFile, conn.execute().bodyStream())
    }

  private fun writeFile(localFile: File, stream: BufferedInputStream): File {
    val buffer = ByteArray(1024)
    val byteOutputStream = ByteArrayOutputStream()
    var len = stream.read(buffer)
    while (len != -1) {
      byteOutputStream.write(buffer, 0, len)
      len = stream.read(buffer)
    }
    localFile.writeBytes(byteOutputStream.toByteArray())
    stream.close()
    byteOutputStream.close()
    return localFile
  }

  enum class ApiVersion(val path: String) {
    V1("/api/v1"),
    V2("/api/v2");
    fun getAddress(): String = "$BACKEND_ADDRESS$path"
  }

  override val configPrefix = ""
  override val priority: Int = 9
  override fun init() {
    Arona.globalEventChannel().filter { it is BaseDatabaseInitEvent }.subscribeOnce<BaseDatabaseInitEvent> { _ ->
      registerInstance()
    }
  }
}