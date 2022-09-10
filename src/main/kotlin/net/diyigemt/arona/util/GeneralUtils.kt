package net.diyigemt.arona.util

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.diyigemt.arona.Arona
import net.diyigemt.arona.advance.AronaUpdateChecker
import net.diyigemt.arona.command.CallMeCommand
import net.diyigemt.arona.command.TrainerCommand
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.image.ImageTable
import net.diyigemt.arona.db.image.ImageTableModel
import net.diyigemt.arona.db.name.TeacherName
import net.diyigemt.arona.db.name.TeacherNameTable
import net.diyigemt.arona.entity.ServerResponse
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.util.NetworkUtil.BACKEND_ADDRESS
import net.diyigemt.arona.util.NetworkUtil.baseRequest
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.jetbrains.exposed.sql.and
import org.jsoup.Connection
import org.jsoup.Connection.Response
import org.jsoup.Jsoup
import java.io.File
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

object GeneralUtils: InitializedFunction() {

  private const val IMAGE_FOLDER = "/image"
  private const val BACKEND_IMAGE_RESOURCE = "${BACKEND_ADDRESS}$IMAGE_FOLDER"

  fun checkService(group: Contact?): Boolean = when(group) {
    is Group -> AronaConfig.groups.contains(group.id)
    else -> false
  }

  fun clearExtraQute(s: String): String {
    if (s.replace("\"", "").length + 2 == s.length) {
      return s.replaceFirst("\"", "").substring(0, s.length - 2)
    }
    return s
  }

  fun queryTeacherNameFromDB(contact: Contact, user: UserOrBot): String {
    if (!CallMeCommand.enable) return user.nameCardOrNick
    val name = query {
      TeacherName.find { (TeacherNameTable.group eq contact.id) and (TeacherNameTable.id eq user.id) }.firstOrNull()
    }?.name ?: user.nameCardOrNick
    return if (AronaConfig.endWithSensei.isNotBlank() && !name.endsWith(AronaConfig.endWithSensei)) "${name}${AronaConfig.endWithSensei}" else name
  }

  fun randomInt(bound: Int): Int = (System.currentTimeMillis() % bound).toInt()

  fun randomBoolean(): Boolean = System.currentTimeMillis().toString().let {
    it.substring(it.length - 1).toInt() % 2 == 0
  }

  suspend fun uploadChapterHelper() {
    doUpload("/map-cache")
  }

  suspend fun uploadStudentInfo() {
    doUpload("/student_info")
  }

  private suspend fun doUpload(path: String) {
    val imageFileList = File(Arona.dataFolderPath() + path).listFiles() ?: return
    val g = Arona.arona.groups[1002484182]!!
    imageFileList.forEach {
      val name = it.name
      val res = it.toExternalResource("png")
      val upload = g.uploadImage(res)
      val msg = g.sendMessage(upload)
      Arona.info("$name ${msg.source.originalMessage.serializeToMiraiCode()}")
      Thread.sleep(1000)
      res.closed
    }
  }

  fun loadImageOrUpdate(name: String): File? {
    // TODO 查本地数据库比对hash
    val localDB = query {
      ImageTableModel.find { ImageTable.name eq name }.firstOrNull()
    }
    val result = kotlin.runCatching {
      NetworkUtil.requestImage(name)
    }.onFailure {
      // 服务器寄了 尝试从本地拿
      if (localDB != null) {
        val localFile = localImageFile(localDB.path)
        return if (localFile.exists()) localFile else null
      }
      return null
    }.getOrNull() ?: return null
    val imageResult = result.data
    val localFile = localImageFile(imageResult.path)
    // 没有本地图片, 向后端下载并存入数据库中
    return if (localDB == null) {
      imageRequest(imageResult.path, localFile)
      // 将本地图片信息写入数据库
      query {
        ImageTableModel.new {
          this.name = name
          this.path = imageResult.path
          this.hash = imageResult.hash
        }
      }
      localFile
    } else {
      // 有本地图片 如果本地hash值与服务器不一致或者本地文件不存在则获取图片
      return if (localDB.hash != imageResult.hash || !localFile.exists()) {
        // 删除本地图片并重新获取
        if (localDB.path != imageResult.path) {
          localImageFile(localDB.path).delete()
        }
        // 否则直接写入旧文件
        imageRequest(imageResult.path, localFile)
        // 更新hash
        query {
          localDB.hash = imageResult.hash
          localDB.path = imageResult.path
        }
        localFile
      } else {
        localFile
      }
    }
  }
  private fun imageRequest(path: String, localFile: File): File {
    val connection = baseRequest(path, BACKEND_IMAGE_RESOURCE)
    val res = connection.execute().bodyStream().readAllBytes()
    localFile.writeBytes(res)
    return localFile
  }

  private fun imageFileFolder(subFolder: String = "") = Arona.dataFolderPath(IMAGE_FOLDER) + subFolder

  private fun localImageFile(path: String) = File(imageFileFolder(path.let { return@let if (path.startsWith("/")) path else "/$it" }))

  override fun init() {
    // 初始化本地图片文件夹
    File(imageFileFolder(TrainerCommand.ChapterMapFolder)).also { it.mkdirs() }
    File(imageFileFolder(TrainerCommand.StudentRankFolder)).also { it.mkdirs() }
  }
}