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
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File

object NetworkUtil {
  const val BACKEND_ADDRESS = "https://arona.diyigemt.com"
  private const val CDN_ADDRESS = "https://arona.cdn.diyigemt.com"
//  const val BACKEND_ADDRESS = "http://localhost:12201"
  private const val BACKEND_API_ADDRESS = "$BACKEND_ADDRESS/api/v1"
  const val BACKEND_IMAGE_FOLDER = "/image"
  private const val BACKEND_FILE_FOLDER = "/file"
  private const val BACKEND_FILE_RESOURCE = "${BACKEND_ADDRESS}$BACKEND_FILE_FOLDER"
  private const val AUTH_HEADER = "Authorization"
  private const val VERSION_HEADER = "version"

  // 向后端服务器注册自己
  fun registerInstance() {
    val sysSave = SysDataUtil.get(SysStatic.UUID)
    if (sysSave == null) {
      // 清除旧的配置文件
      AronaConfig.uuid = ""
      sendDataToServerSource("/user/register")
        .onSuccess {
          val header = it.header(AUTH_HEADER)
          if (header.isNullOrBlank()) {
            Arona.warning("register failure")
            return
          }
          SysDataUtil.saveRegisterData(header)
          AronaConfig.uuid = header
          Arona.info("register success")
        }.onFailure {
          Arona.warning("register failure")
        }
    } else {
      AronaConfig.uuid = sysSave
    }
  }

  fun logoutInstance() {
    sendDataToServerSource("/user/offline")
  }

  fun requestImage(name: String): Result<ServerResponse<List<ImageResult>>> =
    fetchDataFromServer(BACKEND_IMAGE_FOLDER, mapOf("name" to name))

  inline fun <reified T> fetchDataFromServer(
    api: String,
    data: Map<String, String> = mutableMapOf()
  ): Result<ServerResponse<T>> =
    fetchDataFromServerSource(api, data).map {
      Json.decodeFromString(it.body())
    }

  fun fetchDataFromServerSource(api: String, data: Map<String, String> = mutableMapOf()): Result<Response> =
    kotlin.runCatching {
      baseRequest(api)
        .data(data)
        .execute()
    }

  private inline fun <reified T> sendDataToServer(
    api: String,
    data: Map<String, String> = mutableMapOf()
  ): Result<ServerResponse<T>> = sendDataToServerSource(api, data).map {
    Json.decodeFromString(it.body())
  }

  private fun sendDataToServerSource(api: String, data: Map<String, String> = mutableMapOf()): Result<Response> =
    kotlin.runCatching {
      baseRequest(api)
        .data(data)
        .method(Connection.Method.POST)
        .execute()
    }

  private fun baseRequest(api: String, source: String = BACKEND_API_ADDRESS): Connection =
    request("$source${api}")

  private fun cdnRequest(api: String, source: String = CDN_ADDRESS): Connection =
    request("$source${api}")


  private fun request(api: String): Connection =
    Jsoup.connect(api)
    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
    .ignoreContentType(true)
    .header(AUTH_HEADER, AronaConfig.uuid) // 提供身份识别token
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
}