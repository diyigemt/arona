package net.diyigemt.arona.extension

import net.diyigemt.arona.Arona
import net.diyigemt.arona.service.AronaManageService
import net.diyigemt.arona.service.AronaService
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.command.resolve.CommandCallInterceptor
import net.mamoe.mirai.console.command.resolve.InterceptResult
import net.mamoe.mirai.console.command.resolve.InterceptedReason
import net.mamoe.mirai.console.extensions.CommandCallInterceptorProvider
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Message

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
object CommandResolver: CommandCallInterceptorProvider {
  override val instance: CommandCallInterceptor
    get() = CommandResolverInterceptor
}

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
private object CommandResolverInterceptor: CommandCallInterceptor {
  override fun interceptCall(call: CommandCall): InterceptResult<CommandCall>? {
    CommandInterceptorManager.emitInterceptCall(call)
    return super.interceptCall(call)
  }

  override fun interceptBeforeCall(message: Message, caller: CommandSender): InterceptResult<Message>? {
    val unreliableCommandName = extraCommandName(message)
    val matchCommand = CommandManager.matchCommand(unreliableCommandName)
    if (matchCommand is AronaService && caller is UserCommandSender) {
      if (!matchCommand.enable) {
        Arona.runSuspend {
          caller.sendMessage("功能未启用")
        }
        return InterceptResult(InterceptedReason("功能未启用"))
      } else if (matchCommand is AronaManageService) {
        if (!matchCommand.checkAdmin(caller.user, caller.subject)) {
          return InterceptResult(InterceptedReason("权限不足"))
        }
      }
    }
    val reason = CommandInterceptorManager.emitInterceptBeforeCall(message, caller)
    return if (reason != null) {
      InterceptResult(InterceptedReason(reason))
    } else {
      super.interceptBeforeCall(message, caller)
    }
  }
  
  private fun extraCommandName(message: Message): String {
    val contentToString = message.contentToString().split(" ")
    return contentToString[0]
  }
}