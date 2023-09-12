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
import net.diyigemt.arona.config.*
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.extension.CommandInterceptorManager
import net.diyigemt.arona.extension.CommandResolver
import net.diyigemt.arona.handler.GroupRepeaterHandler
import net.diyigemt.arona.handler.HentaiEventHandler
import net.diyigemt.arona.handler.NudgeEventHandler
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.quartz.QuartzProvider
import net.diyigemt.arona.remote.RemoteServiceManager
import net.diyigemt.arona.service.AronaServiceManager
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.ImageUtil
import net.diyigemt.arona.util.NetworkUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.UserOrBot
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.info
import java.io.File
import kotlin.io.path.absolutePathString

object Arona : KotlinPlugin(
  JvmPluginDescription.loadFromResource()
) {
  var arona: Bot? = null
    set(value) {
      field = value
      if (value != null && aronaTaskList.isNotEmpty()) {
        aronaTaskList.forEach {
          runSuspend {
            it(value)
          }
        }
      }
    }
  private val aronaTaskList = mutableListOf<suspend (arona: Bot) -> Unit>()
  private val INIT: List<InitializedFunction> =
    listOf(
      GeneralUtils,
      AronaServiceManager,
      CommandInterceptorManager,
      RemoteServiceManager,
      ImageUtil
    )

  @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
  override fun onEnable() {
    init()
    if (DataBaseProvider.isConnected()) {
      globalEventChannel().filter {
        it is BotOnlineEvent && it.bot.id == AronaConfig.qq
      }.subscribeOnce<BotOnlineEvent> {
        arona = it.bot
        if (AronaConfig.sendOnlineMessage) {
          sendMessage(deserializeMiraiCode(AronaConfig.onlineMessage))
        }
      }
      globalEventChannel().subscribeAlways<NudgeEvent> {
        NudgeEventHandler.preHandle(this)
      }
      globalEventChannel().subscribeAlways<GroupMessageEvent> {
        GroupRepeaterHandler.preHandle(this)
        HentaiEventHandler.preHandle(this)
      }
      logger.info { "arona loaded" }
    } else error("arona database init failed, arona will not start")
  }

  @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
  override fun PluginComponentStorage.onLoad() {
    contributeCommandCallParser(CommandResolver)
  }

  private fun init() {
    AronaConfig.reload()
    AronaGachaConfig.init()
    AronaNudgeConfig.reload()
    AronaHentaiConfig.reload()
    AronaRepeatConfig.reload()
    AronaNotifyConfig.reload()
    NGAPushConfig.reload()
    AronaTarotConfig.reload()
    AronaEmergencyConfig.reload()
    AronaTrainerConfig.reload()
    DataBaseProvider.start()
    QuartzProvider.start()
    runSuspend {
      INIT.forEach {
        it.init()
      }
      NetworkUtil.registerInstance()
    }
//    startUpload() // 上传图片获取mirai-code
  }

  override fun onDisable() {
    AronaConfig.save()
    AronaNudgeConfig.save()
    AronaGachaConfig.save()
    AronaHentaiConfig.save()
    AronaRepeatConfig.save()
    AronaNotifyConfig.save()
    AronaServiceConfig.save()
    AronaTarotConfig.save()
    AronaEmergencyConfig.save()
    AronaTrainerConfig.save()
    AronaServiceManager.saveServiceStatus()
  }

  fun runSuspend(block: suspend () -> Unit) = launch(coroutineContext) {
    block()
  }

  fun runAsync(block: suspend (event: EventChannel<Event>) -> Unit) = async(coroutineContext) {
    block(globalEventChannel())
  }

  fun sendExitMessage() {
    if (AronaConfig.sendOfflineMessage) {
      sendMessage(deserializeMiraiCode(AronaConfig.offlineMessage))
    }
  }

  fun sendMessage(message: String) {
    runWithArona {
      AronaConfig.groups.forEach { group0 ->
        val group = it.groups[group0] ?: return@forEach
        group.sendMessage(message)
      }
    }
  }

  /**
   * 发送带文件的消息
   * @param delay 每个群的延迟时间, 单位为秒
   */
  fun sendMessageWithFile(delay: Int = 0, block: suspend (group: Contact) -> Message?) {
    runWithArona {
      AronaConfig.groups.forEach { group0 ->
        val group = it.groups[group0] ?: return@forEach
        val message = block(group)
        if (message != null) {
          group.sendMessage(message)
          Thread.sleep(delay * 1000L)
        }
      }
    }
  }

  fun sendFilterGroupMessageWithFile(groups: List<Long>?, block: suspend (group: Contact) -> MessageChain) {
    if (groups.isNullOrEmpty()) {
      sendMessageWithFile(0, block)
    } else {
      runWithArona {
        groups
          .intersect(AronaConfig.groups.toSet())
          .forEach { group0 ->
            val group = it.groups[group0] ?: return@forEach
            val message = block(group)
            group.sendMessage(message)
          }
      }
    }
  }

  fun sendMessage(message: MessageChain) {
    runWithArona {
      AronaConfig.groups.forEach { group0 ->
        val group = it.groups[group0] ?: return@forEach
        group.sendMessage(message)
      }
    }
  }

  fun sendMessage(messageBuilder: (group: Group) -> MessageChain) {
    runWithArona {
      AronaConfig.groups.forEach { group0 ->
        val group = it.groups[group0] ?: return@forEach
        group.sendMessage(messageBuilder(group))
      }
    }
  }

  fun sendMessageToAdmin(message: String) {
    runWithArona { arona ->
      fun getAdmin(id: Long): NormalMember? {
        AronaConfig.groups.forEach {
          val admin = arona.groups[it]?.get(id)
          if (admin != null) return admin
        }
        return null
      }
      run {
        if (AronaConfig.groups.isEmpty() || AronaConfig.managerGroup.isEmpty()) return@run
        val list = mutableListOf<NormalMember>()
        AronaConfig.managerGroup.forEach {
          val admin = getAdmin(it) ?: return@forEach
          list.add(admin)
        }
        list.forEach {
          it.sendMessage(message)
        }
      }
    }
  }

  private fun runWithArona(block: suspend (arona: Bot) -> Unit) {
    if (arona != null) {
      runSuspend {
        block(arona!!)
      }
    } else {
      aronaTaskList.add(block)
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
