package net.diyigemt.arona.extension

import net.diyigemt.arona.command.ActivityCommand
import net.diyigemt.arona.interfaces.InitializedFunction
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Message

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
object CommandInterceptorManager: InitializedFunction() {
  private val ITEMS: MutableList<CommandInterceptor> = mutableListOf()
  override fun init() {
    ITEMS.add(ActivityCommand)
    ITEMS.add(ExitCommandInterceptor)
    ITEMS.sortBy { it.level }
  }

  fun emitInterceptCall(call: CommandCall) {
    ITEMS.forEach {
      if (!it.interceptCall(call)) return@forEach
    }
  }

  fun emitInterceptBeforeCall(message: Message, caller: CommandSender): String? {
    ITEMS.forEach {
      val reason = it.interceptBeforeCall(message, caller)
      if (reason != null) return reason
    }
    return null
  }
}