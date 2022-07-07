package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.service.AronaManageService
import net.diyigemt.arona.service.AronaServiceManager
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender

object AronaConfigCommand: CompositeCommand(
  Arona,
  "config",
  "配置",
  description = "配置arona运行状态"
), AronaManageService {

  @SubCommand("启用")
  @Description("启用一个功能模块")
  suspend fun UserCommandSender.enableService(idOrName: String) {
    if (!checkAdmin(user, subject)) return
    val service = AronaServiceManager.enable(idOrName)
    if (service != null) {
      sendMessage("功能${service.name}启用成功")
    } else {
      sendMessage("服务未找到")
    }
  }

  @SubCommand("停用")
  @Description("停用一个功能模块")
  suspend fun UserCommandSender.disableService(idOrName: String) {
    if (!checkAdmin(user, subject)) return
    val service = AronaServiceManager.disable(idOrName)
    if (service != null) {
      sendMessage("功能${service.name}停用成功")
    } else {
      sendMessage("服务未找到")
    }
  }

  override val id: Int = 0
  override val name: String = "配置"
  override var enable: Boolean = true
  override fun init() {
    registerService()
  }

}