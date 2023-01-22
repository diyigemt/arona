package net.diyigemt.arona.handler

import kotlinx.serialization.Serializable
import net.diyigemt.arona.config.GlobalConfigProvider
import net.diyigemt.arona.entity.BotGroupConfig
import net.diyigemt.arona.interfaces.ConfigReader
import net.diyigemt.arona.interfaces.getConfig
import net.diyigemt.arona.interfaces.getGroupConfig
import net.diyigemt.arona.interfaces.getGroupConfigOrDefault
import net.diyigemt.arona.service.AronaGroupService
import net.diyigemt.arona.service.AronaMessageReactService
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.executeCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType

// 复指令重定向
object MessageEventDispatcher:
  AronaMessageReactService<MessageEvent>,
  AronaGroupService,
  ConfigReader
{
  @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
  override suspend fun handle(event: MessageEvent) {
    val command = event.message.firstOrNull { it is PlainText }?.contentToString() ?: return
    val subjectId = event.subject.id
    val prefix = getGroupConfigOrDefault("prefix", subjectId, CommandManager.commandPrefix)
    if (!command.startsWith(prefix)) {
      return
    }
    val config = getGroupConfig<List<CommandRedirectConfig>>("config", subjectId, List::class.createType(listOf(
      KTypeProjection.invariant(CommandRedirectConfig::class.starProjectedType)
    )))
    if (config.isEmpty()) {
      return
    }
    val active = config.filter { prefix + it.source == command }
    if (active.isEmpty()) {
      return
    }
    val contentString = event.message.serializeToMiraiCode()
    val commandSender = event.toCommandSender()
    active.map {
      prefix + it.target + contentString.replace(prefix + it.source, "")
    }.forEach {
      commandSender.executeCommand(it)
    }
  }

  @Serializable
  data class CommandRedirectConfig(
    val source: String,
    val target: String
  )

  override val event = MessageEvent::class

  override val id: Int = 31
  override val name: String = "指令重定向"
  override var isGlobal: Boolean = true
  override val description: String = name
  override val configPrefix = "dispatcher"

}