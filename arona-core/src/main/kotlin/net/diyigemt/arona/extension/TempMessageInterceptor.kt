package net.diyigemt.arona.extension

import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.service.AronaGroupService
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Message

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
object TempMessageInterceptor: CommandInterceptor {
  override val level: Int = 1

  override fun interceptBeforeCall(message: Message, caller: CommandSender): String? {
    val unreliableCommandName = extraCommandName(message)
    val matchCommand = CommandManager.matchCommand(unreliableCommandName)
    if (matchCommand !is AronaGroupService && caller is UserCommandSender) {
      val callerId = caller.user.id
      if (AronaConfig.managerGroup.contains(callerId)) {
        return null
      }
      return when(AronaConfig.tempMessageIgnoreType) {
        TempMessageIgnoreType.NONE -> {
          null
        }
        TempMessageIgnoreType.ONLY_SERVICE_GROUP -> {
          if (caller.bot.groups.any { group -> group.contains(callerId) }) {
            return null
          }
          "忽略非服务群友私聊"
        }
        TempMessageIgnoreType.ALL -> {
          "忽略所有群聊"
        }
      }
    }
    return null
  }
}