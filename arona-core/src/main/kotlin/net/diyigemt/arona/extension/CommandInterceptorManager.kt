package net.diyigemt.arona.extension

import net.diyigemt.arona.interfaces.Initialize
import net.diyigemt.arona.util.ReflectionUtil
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Message

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
object CommandInterceptorManager: Initialize {
  private val ITEMS: MutableList<CommandInterceptor> = mutableListOf()
  fun registerItem(item: CommandInterceptor) {
    ITEMS.add(item)
    ITEMS.sortBy { it.level }
  }

  fun emitInterceptCall(call: CommandCall) {
    run loop@ {
      ITEMS.forEach {
        if (!it.interceptCall(call)) return@loop
      }
    }
  }

  fun emitInterceptBeforeCall(message: Message, caller: CommandSender): String? {
    ITEMS.forEach {
      val reason = it.interceptBeforeCall(message, caller)
      if (reason != null) return reason
    }
    return null
  }

  override fun init() {
    ReflectionUtil.getInterfacePetObjectInstance<CommandInterceptor>().forEach {
      registerItem(it)
    }
  }
}