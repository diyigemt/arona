package net.diyigemt.arona.extension

import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.ActivityCommand
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.config.AronaNotifyConfig
import net.diyigemt.arona.entity.ServerLocale
import net.diyigemt.arona.util.GeneralUtils
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.command.resolve.CommandCallInterceptor
import net.mamoe.mirai.console.command.resolve.InterceptResult
import net.mamoe.mirai.console.command.resolve.InterceptedReason
import net.mamoe.mirai.console.command.resolve.ResolvedCommandCall
import net.mamoe.mirai.console.extensions.CommandCallInterceptorProvider
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Message
import kotlin.reflect.typeOf

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
object CommandResolver: CommandCallInterceptorProvider {
  override val instance: CommandCallInterceptor
    get() = CommandResolverInterceptor
}

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
private object CommandResolverInterceptor: CommandCallInterceptor {
  private val ACTIVITY_COMMAND = "${CommandManager.commandPrefix}活动"
  private val EXIT_COMMAND = listOf(
    "${CommandManager.commandPrefix}stop",
    "${CommandManager.commandPrefix}shutdown",
    "${CommandManager.commandPrefix}exit"
  )
  override fun interceptCall(call: CommandCall): InterceptResult<CommandCall>? {
    val calleeName = call.calleeName
    val valueArguments = call.valueArguments
    return when {
      valueArguments.isEmpty() ->
        when {
          EXIT_COMMAND.contains(calleeName) -> {
            Arona.sendExitMessage()
            super.interceptCall(call)
          }
          else -> super.interceptCall(call)
      }
      else -> super.interceptCall(call)
    }
  }

  override fun interceptBeforeCall(message: Message, caller: CommandSender): InterceptResult<Message>? {
    return when (message.contentToString()) {
      ACTIVITY_COMMAND -> {
        if ((caller is UserCommandSender) and GeneralUtils.checkService(caller.subject)) {
          val subject = caller.subject!!
          if (AronaConfig.defaultActivityCommandServer == ServerLocale.JP) {
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
          InterceptResult(InterceptedReason(""))
        } else {
          super.interceptBeforeCall(message, caller)
        }
      }
      else -> super.interceptBeforeCall(message, caller)
    }
  }
}