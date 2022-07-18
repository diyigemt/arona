package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.service.AronaService
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender

object TarotCommand : SimpleCommand(
  Arona,"tarot", "塔罗牌",
  description = "抽一张塔罗牌"
), AronaService {

  @Handler
  suspend fun UserCommandSender.tarot() {

  }

  override val id: Int = 16
  override val name: String = "塔罗牌"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }

}