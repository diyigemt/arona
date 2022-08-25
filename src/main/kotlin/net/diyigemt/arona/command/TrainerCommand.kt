package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.util.GeneralUtils
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode

object TrainerCommand : SimpleCommand(
  Arona,"trainer", "攻略",
  description = "主线地图攻略"
), AronaService {
  private val MapRegex: Regex = Regex("^([1-9]\\d?-[1-5])|(H[1-9]\\d?-[1-3])$")
  @Handler
  suspend fun UserCommandSender.trainer(mapId: String) {
    val match = MapRegex.matchEntire(mapId).also {
      if (it == null) {
        subject.sendMessage("地图名称错误")
        return
      }
    }
    val group = match!!.groupValues
    val id = group[0]
    val response = GeneralUtils.fetchDataFromServer<String>("/main-map?id=${id}")
    sendMessage(response.data.deserializeMiraiCode())
  }

  override val id: Int = 20
  override val name: String = "地图攻略"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }

}