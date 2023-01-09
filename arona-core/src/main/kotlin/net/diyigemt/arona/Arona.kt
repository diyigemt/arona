/*
 * Copyright 2020-2021 StageGuard.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/diyigemt/arona/blob/master/LICENSE
 */
package net.diyigemt.arona

import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.diyigemt.arona.annotations.HideService
import net.diyigemt.arona.config.GlobalConfigProvider
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.entity.BotGroupConfig
import net.diyigemt.arona.extension.CommandResolver
import net.diyigemt.arona.interfaces.*
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.service.AronaServiceManager
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.ReflectionUtil
import net.diyigemt.arona.web.WebUIService
import net.diyigemt.arona.web.blockly.BlocklyService
import net.diyigemt.arona.web.blockly.SaveManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.AbstractCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.executeCommand
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService.Companion.getPermittedPermissions
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.UserOrBot
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import java.io.File
import kotlin.io.path.absolutePathString
import kotlin.reflect.full.hasAnnotation

object Arona : KotlinPlugin(
  JvmPluginDescription.loadFromResource()
), ConfigReader {
  private const val CommandPrefix = "/permission add * net.diyigemt.arona:command."
  @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
  override fun onEnable() {
    init()
    System.setProperty("java.awt.headless", "true")
    val pluginEventChannel = globalEventChannel()
    val botConfig = getMainConfig<List<BotGroupConfig>>("bots")
    val botList = botConfig.map { it.bot }
    pluginEventChannel.filter {
      it is BotOnlineEvent && botList.contains(it.bot.id)
    }.subscribeAlways<BotOnlineEvent> {
      val botId = it.bot.id
      val groups = botConfig
        .filter { config -> config.bot == botId }
        .map { config -> config.groups }
        .flatMap { config -> config.toList() }
      groups.forEach { group ->
        val message = getGroupConfig<String>("onlineMessage", group)
          .also { message -> if(message.isBlank()) return@forEach }
        sendGroupMessage(group) {
          this.add(message)
          this.build()
        }
      }
    }
    pluginEventChannel.subscribeAlways<BotEvent> {
      AronaServiceManager.emit(this)
    }
    info { "arona loaded" }
  }

  @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
  override fun PluginComponentStorage.onLoad() {
    contributeCommandCallParser(CommandResolver)
  }

  private fun init() {
//    WebUIService.init()
//    WebUIService.enableService()
//    BlocklyService.init()
    //TODO
    // 查找所有需要初始化的类, 所有指令, 所有配置文件

    // 需要初始化的类
    runSuspend {
      ReflectionUtil.getInterfacePetObjectInstance<Initialize>().sortedBy { it.priority }.forEach {
        it.init()
      }
    }
    // 需要协程的类
    ReflectionUtil.getInterfacePetObjectInstance<CoroutineFunctionProvider>().forEach {
      it.start()
    }
//    val grantCommandName = mutableListOf<String>()
//    // 注册service
    ReflectionUtil.getInterfacePetObjectInstance<AronaService>().forEach {
//      // 向控制台注册指令
//      if (it is AbstractCommand) {
//        it.register()
//        // 直接提供指令执行权限
//        if (!it::class.hasAnnotation<HideService>()) {
//          grantCommandName.add(it.primaryName)
//        }
//      }
      AronaServiceManager.register(it)
    }

    // 自动赋予指令权限
    //TODO
//    if (AronaConfig.autoGrantPermission) {
//      // 获取已经赋予的权限
//      val grantedList = AbstractPermitteeId.AnyContact.getPermittedPermissions().toList()
//      grantCommand(grantCommandName.filter {
//        !grantedList.any {
//            already -> already.id.toString().endsWith(it)
//        }
//      })
//    }

  }

  override fun onDisable() {
    WebUIService.disableService()
  }

  @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
  private fun grantCommand(name: List<String>) {
    runSuspend {
      name.forEach { n ->
        ConsoleCommandSender.executeCommand("$CommandPrefix$n")
      }
    }
  }

  fun runSuspend(block: suspend () -> Unit) = launch(coroutineContext) {
    block()
  }

  fun <R> async(block: suspend () -> R) = async(coroutineContext) {
    block()
  }

  fun sendExitMessage() {
    val botConfig = getMainConfig<List<BotGroupConfig>>("bots")
    val groups = botConfig
      .map { it.groups }
      .flatMap { it.toList() }
      .filter { group ->
        getGroupConfig<String>("offlineMessage", group).isNotBlank()
      }
    groups.forEach { group ->
      val message = getGroupConfig<String>("offlineMessage", group)
      sendGroupMessage(group) {
        this.add(message)
        this.build()
      }
    }
  }

  fun sendGroupMessage(group0: Long, block: suspend (MessageChainBuilder.(group: Contact) -> MessageChain)) {
    runSuspend {
      val botConfig = getMainConfig<List<BotGroupConfig>>("bots")
      val bot0 = botConfig.firstOrNull { it.groups.contains(group0) }?.bot ?: return@runSuspend
      val bot = Bot.getInstanceOrNull(bot0) ?: return@runSuspend
      val group = bot.getGroup(group0) ?: return@runSuspend
      group.sendMessage(block(MessageChainBuilder(), group))
    }
  }

  fun sendMessageToAdmin(message: String) {
    val botConfig = getMainConfig<List<BotGroupConfig>>("bots")
    val managerGroup = getMainConfig<List<Long>>("managerGroup")
    if (botConfig.isEmpty() || managerGroup.isEmpty()) {
      return
    }
    val botList = botConfig.map { it.bot }
    val bots = botList.mapNotNull { Bot.getInstanceOrNull(it) }
    runSuspend {
      managerGroup
        .mapNotNull {
          bots.firstOrNull { bot ->
            bot.friends.firstOrNull { friend ->
              friend.id == it
            } != null
          }?.friends?.first { friend ->
            friend.id == it
          }
        }
        .forEach {
          it.sendMessage(message)
        }
    }
  }

  fun dataFolderPath(subPath: String = ""): String = Arona.dataFolderPath.absolutePathString() + subPath

  fun dataFolderFile(subPath: String = ""): File = File(Arona.dataFolderPath.absolutePathString() + subPath)

  suspend fun Group.sendTeacherNameMessage(user: UserOrBot, message: String) {
    val name = GeneralUtils.queryTeacherNameFromDB(this, user)
    this.sendMessage(message.replace("\${teacherName}", name))
  }

  suspend fun Group.sendTeacherNameMessage(user: UserOrBot, message: MessageChain) {
    val name = GeneralUtils.queryTeacherNameFromDB(this, user)
    val s = message.serializeToMiraiCode().replace("\${teacherName}", name).deserializeMiraiCode()
    this.sendMessage(s)
  }

  suspend fun CommandSender.sendTeacherNameMessage(user: UserOrBot, message: MessageChain) {
    val name = GeneralUtils.queryTeacherNameFromDB(this.subject?.id ?: 0L, user)
    val s = message.serializeToMiraiCode().replace("\${teacherName}", name).deserializeMiraiCode()
    this.sendMessage(s)
  }

  fun info(message: String?) = logger.info(message)

  fun warning(message: String?) = logger.warning(message)

  fun error(message: String?) = logger.error(message)

  fun verbose(message: String?) = logger.verbose(message)

  fun info(message: () -> String?) = logger.info(message())

  fun warning(message: () -> String?) = logger.warning(message())

  fun verbose(message: () -> String?) = logger.verbose(message())

  fun error(message: () -> String?) = logger.error(message())
  override val configPrefix: String = ""

}