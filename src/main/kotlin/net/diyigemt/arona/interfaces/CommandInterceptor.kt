package net.diyigemt.arona.interfaces

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Message
@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
abstract class CommandInterceptor() {
  open val level: Int = 1
  open fun interceptCall(call: CommandCall): Boolean = true
  open fun interceptBeforeCall(message: Message, caller: CommandSender): String? = null
}