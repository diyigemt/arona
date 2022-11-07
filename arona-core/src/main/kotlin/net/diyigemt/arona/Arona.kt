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
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.config.AronaServiceConfig
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.extension.CommandResolver
import net.diyigemt.arona.interfaces.CoroutineFunctionProvider
import net.diyigemt.arona.interfaces.Initialize
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.service.AronaServiceManager
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.NetworkUtil
import net.diyigemt.arona.util.ReflectionUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.AbstractCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
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
) {
  var arona: Bot? = null
  private const val CommandPrefix = "/permission add * net.diyigemt.arona:command."
  @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
  override fun onEnable() {
    init()
    System.setProperty("java.awt.headless", "true")
    if (DataBaseProvider.isConnected()) {
      val pluginEventChannel = globalEventChannel()
      pluginEventChannel.filter {
        it is BotOnlineEvent && it.bot.id == AronaConfig.qq
      }.subscribeOnce<BotOnlineEvent> {
        arona = it.bot
        if (AronaConfig.sendOnlineMessage) {
          sendMessage(deserializeMiraiCode(AronaConfig.onlineMessage))
        }
      }
      pluginEventChannel.subscribeAlways<BotEvent> {
        AronaServiceManager.emit(this)
      }
      info { "arona loaded" }
    } else error("arona database init failed, arona will not start")
  }

  @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
  override fun PluginComponentStorage.onLoad() {
    contributeCommandCallParser(CommandResolver)
  }

  private fun init() {
    // 查找所有需要初始化的类, 所有指令, 所有配置文件
    // 重载配置文件
    ReflectionUtil.getInterfacePetObjectInstance<AutoSavePluginConfig>().forEach {
      it.reload()
    }
    // 需要协程的类
    ReflectionUtil.getInterfacePetObjectInstance<CoroutineFunctionProvider>().forEach {
      it.start()
    }
    val grantCommandName = mutableListOf<String>()
    // 注册service
    ReflectionUtil.getInterfacePetObjectInstance<AronaService>().forEach {
      // 向控制台注册指令
      if (it is AbstractCommand) {
        it.register()
        // 直接提供指令执行权限
        if (!it::class.hasAnnotation<HideService>()) {
          grantCommandName.add(it.primaryName)
        }
      }
      AronaServiceManager.register(it)
    }

    // 需要初始化的类
    runSuspend {
      ReflectionUtil.getInterfacePetObjectInstance<Initialize>().sortedBy { it.priority }.forEach {
        it.init()
      }
    }

    // 自动赋予指令权限
    if (AronaConfig.autoGrantPermission) {
      // 获取已经赋予的权限
      val grantedList = AbstractPermitteeId.AnyContact.getPermittedPermissions().toList()
      grantCommand(grantCommandName.filter {
        !grantedList.any {
            already -> already.id.toString().endsWith(it)
        }
      })
    }

    runSuspend {
      NetworkUtil.registerInstance()
    }
  }

  override fun onDisable() {
    ReflectionUtil.getInterfacePetObjectInstance<AutoSavePluginConfig>().forEach {
      it.save()
    }
    AronaServiceManager.saveServiceStatus()
    AronaServiceConfig.save()
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
    if (AronaConfig.sendOfflineMessage) {
      sendMessage(deserializeMiraiCode(AronaConfig.offlineMessage))
    }
  }

  fun sendMessageWithFile(block: suspend (group: Contact) -> MessageChain) {
    runSuspend {
      AronaConfig.groups.forEach {
        val group = arona?.groups?.get(it) ?: return@forEach
        group.sendMessage(block(group))
      }
    }
  }

  fun sendMessage(message: String) {
    val builder = MessageChainBuilder()
    builder.add(message)
    sendMessage(builder.build())
  }

  fun sendMessage(message: MessageChain) {
    runSuspend {
      AronaConfig.groups.forEach {
        val group = arona?.groups?.get(it) ?: return@forEach
        group.sendMessage(message)
      }
    }
  }

  fun sendMessageToAdmin(message: String) {
    fun getAdmin(id: Long): NormalMember? {
      AronaConfig.groups.forEach {
        val admin = arona?.groups?.get(it)?.get(id)
        if (admin != null) return admin
      }
      return null
    }
    runSuspend {
      if (AronaConfig.groups.isEmpty() || AronaConfig.managerGroup.isEmpty()) return@runSuspend
      AronaConfig.managerGroup
        .mapNotNull {
          getAdmin(it)
        }
        .forEach {
          it.sendMessage(message)
        }
    }
  }

  // 用以支持消息撤回功能
  suspend fun Contact.sendMessageSave(msg: String) {
    this.sendMessage(msg)
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

  fun info(message: String?) = logger.info(message)

  fun warning(message: String?) = logger.warning(message)

  fun error(message: String?) = logger.error(message)

  fun verbose(message: String?) = logger.verbose(message)

  fun info(message: () -> String?) = logger.info(message())

  fun warning(message: () -> String?) = logger.warning(message())

  fun verbose(message: () -> String?) = logger.verbose(message())

  fun error(message: () -> String?) = logger.error(message())

}