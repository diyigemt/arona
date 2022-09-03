package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.util.GeneralUtils
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode

object TrainerCommand : SimpleCommand(
  Arona,"trainer", "攻略",
  description = "主线地图和学生攻略"
), AronaService {
  private val MapRegex: Regex = Regex("^([1-9]\\d?-[1-5])|(H[1-9]\\d?-[1-3])$")
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
    val response = GeneralUtils.fetchDataFromServer<String>("/main-map?id=${id}")
    contact.sendMessage(response.data.deserializeMiraiCode())
  }

  private suspend fun sendStudent(contact: Contact, name: String) {
    val response = GeneralUtils.fetchDataFromServer<String>("/student-rank?name=${name}")
    val data = response.data
    // 信息有误
    if (data.length < 5) {
      return
    }
    contact.sendMessage(response.data.deserializeMiraiCode())
  }

  override val id: Int = 20
  override val name: String = "地图与学生攻略"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }

}