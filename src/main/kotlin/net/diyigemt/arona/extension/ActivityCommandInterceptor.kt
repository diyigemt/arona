package net.diyigemt.arona.extension

import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.ActivityCommand
import net.diyigemt.arona.config.AronaNotifyConfig
import net.diyigemt.arona.entity.ServerLocale
import net.diyigemt.arona.interfaces.CommandInterceptor
import net.diyigemt.arona.util.GeneralUtils
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Message

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
object ActivityCommandInterceptor: CommandInterceptor() {
  private val ACTIVITY_COMMAND = "${CommandManager.commandPrefix}活动"
  override fun interceptBeforeCall(message: Message, caller: CommandSender): String? {
    if (message.contentToString() != ACTIVITY_COMMAND) return null
    if ((caller is UserCommandSender) and GeneralUtils.checkService(caller.subject)) {
      val subject = caller.subject!!
      if (AronaNotifyConfig.defaultActivityCommandServer == ServerLocale.JP) {
        kotlin.runCatching {
          Arona.runSuspend {
            ActivityCommand.sendJP(subject)
          }
        }.onFailure {
          Arona.runSuspend {
            subject.sendMessage("指令执行失败")
          }
        }
      } else {
        kotlin.runCatching {
          Arona.runSuspend {
            ActivityCommand.sendEN(subject)
          }
        }.onFailure {
          Arona.runSuspend {
            subject.sendMessage("指令执行失败")
          }
        }
      }
      return ""
    } else {
      return null
    }
  }
}