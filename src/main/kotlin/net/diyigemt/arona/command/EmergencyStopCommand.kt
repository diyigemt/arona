package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaEmergencyConfig
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.service.AronaServiceManager
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import java.util.*

object EmergencyStopCommand : SimpleCommand(
  Arona,"emergency_stop", "紧急停止",
  description = "非管理员投票制停止服务"
), AronaService {
  private var start: Date = Calendar.getInstance().time
  private val vote: MutableSet<Long> = mutableSetOf()
  @Handler
  suspend fun UserCommandSender.emergencyStop() {
    val now = Calendar.getInstance().time
    val id = user.id
    val duration = (now.time - start.time) / 1000 / 60
    if (duration >= AronaEmergencyConfig.duration) {
      start = now
      vote.clear()
      return
    }
    vote.add(id)
    subject.sendMessage("当前:${vote.size}/${AronaEmergencyConfig.times}票, 剩余时间: ${AronaEmergencyConfig.duration - duration}分钟")
    checkExit(this)
  }
  @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
  suspend fun checkExit(sender: CommandSender): Boolean {
    if (vote.size >= AronaEmergencyConfig.times) {
      sender.sendMessage("达到目标票数,紧急停止")
      AronaServiceManager.disableAll()
      return true
    }
    return false
  }

  override val id: Int = 17
  override val name: String = "紧急停止"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }

}