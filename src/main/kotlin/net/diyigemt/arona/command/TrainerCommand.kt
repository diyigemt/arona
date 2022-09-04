package net.diyigemt.arona.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.diyigemt.arona.Arona
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.util.GeneralUtils
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.File

object TrainerCommand : SimpleCommand(
  Arona,"trainer", "攻略",
  description = "主线地图和学生攻略"
), AronaService {
  private val MapRegex: Regex = Regex("^([1-9]\\d?-[1-5])|(H[1-9]\\d?-[1-3])$")
  const val StudentRankFolder: String = "/student_rank"
  const val ChapterMapFolder: String = "/chapter_map"
  @Handler
  suspend fun UserCommandSender.trainer(str: String) {
    if (str == "阿罗娜" || str == "彩奈") {
      subject.sendMessage("阿罗娜已经被老师攻略啦>_<")
      return
    }
    if (str == "黑服") {
      subject.sendMessage("南通爬")
      return
    }
    val match = MapRegex.matchEntire(str)
    // 地图攻略
    if  (match != null) {
      sendMap(subject, match)
    } else {
      // 学生攻略
      sendStudent(subject, str)
    }
  }

  private suspend fun sendMap(contact: Contact, match: MatchResult) {
    val group = match.groupValues
    val id = group[0]
    val file = GeneralUtils.getImageOrDownload(ChapterMapFolder, "${id}.png")
    sendImage(contact, file)
//    val response = GeneralUtils.fetchDataFromServer<String>("/main-map?id=${id}")
//    contact.sendMessage(response.data.deserializeMiraiCode())
  }

  private suspend fun sendStudent(contact: Contact, name: String) {
    // 获取学生名字对应的图片名字
    val response = GeneralUtils.fetchDataFromServer<String>("/student-rank", mutableMapOf(
      "name" to name,
      "version" to "v2"
    ))
    val data = response.data
    // 信息有误
    if (data.length < 5) {
      return
    }
    val file = GeneralUtils.getImageOrDownload(StudentRankFolder, data)
    sendImage(contact, file)
//    contact.sendMessage(response.data.deserializeMiraiCode())
  }

  private suspend fun sendImage(contact: Contact, image: File) {
    val resource = image.toExternalResource("png")
    contact.sendMessage(contact.uploadImage(resource))
    withContext(Dispatchers.IO) {
      resource.close()
    }
  }

  override val id: Int = 20
  override val name: String = "地图与学生攻略"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }

}