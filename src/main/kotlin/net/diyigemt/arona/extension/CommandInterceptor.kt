package net.diyigemt.arona.extension

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Message
@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
interface CommandInterceptor {
  val level: Int
  fun interceptCall(call: CommandCall): Boolean = true
  fun interceptBeforeCall(message: Message, caller: CommandSender): String? = null
  fun registerInterceptor() {
    CommandInterceptorManager.registerItem(this)
  }
}