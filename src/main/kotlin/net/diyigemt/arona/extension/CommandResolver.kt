package net.diyigemt.arona.extension

import net.diyigemt.arona.Arona
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.command.resolve.CommandCallInterceptor
import net.mamoe.mirai.console.command.resolve.InterceptResult
import net.mamoe.mirai.console.extensions.CommandCallInterceptorProvider
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
object CommandResolver: CommandCallInterceptorProvider {
  override val instance: CommandCallInterceptor
    get() = CommandResolverInterceptor
}

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
private object CommandResolverInterceptor: CommandCallInterceptor {
  private val EXIT_COMMAND = listOf<String>(
    "${CommandManager.commandPrefix}stop",
    "${CommandManager.commandPrefix}shutdown",
    "${CommandManager.commandPrefix}exit"
  )
  override fun interceptCall(call: CommandCall): InterceptResult<CommandCall>? {
    val calleeName = call.calleeName
    if (EXIT_COMMAND.contains(calleeName)) {
      Arona.sendExitMessage()
    }
    return super.interceptCall(call)
  }
}