/*
 * Copyright 2020-2021 StageGuard.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/diyigemt/arona/blob/master/LICENSE
 */
package net.diyigemt.arona

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.diyigemt.arona.advance.AronaUpdateCheck
import net.diyigemt.arona.config.*
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.extension.CommandInterceptorManager
import net.diyigemt.arona.extension.CommandResolver
import net.diyigemt.arona.handler.GroupRepeaterHandler
import net.diyigemt.arona.handler.HentaiEventHandler
import net.diyigemt.arona.handler.NudgeEventHandler
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.quartz.ActivityNotify
import net.diyigemt.arona.quartz.QuartzProvider
import net.diyigemt.arona.service.AronaServiceManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.message.code.MiraiCode
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.info

object Arona : KotlinPlugin(
  JvmPluginDescription.loadFromResource()
) {
  private lateinit var arona: Bot
  private val INIT: List<InitializedFunction> =
    listOf(
      AronaServiceManager,
      ActivityNotify,
      CommandInterceptorManager,
      AronaUpdateCheck
    )

  @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
  override fun onEnable() {
    init()
    if (DataBaseProvider.isConnected()) {
      GlobalEventChannel.filter {
        it is BotOnlineEvent && it.bot.id == AronaConfig.qq
      }.subscribeOnce<BotOnlineEvent> {
        arona = it.bot
        if (AronaConfig.sendOnlineMessage) {
          sendMessage(MiraiCode.deserializeMiraiCode(AronaConfig.onlineMessage))
        }
      }
      if (AronaNudgeConfig.enable) {
        GlobalEventChannel.subscribeAlways<NudgeEvent>(priority = AronaNudgeConfig.priority) {
          NudgeEventHandler.preHandle(this)
        }
      }
      GlobalEventChannel.subscribeAlways<GroupMessageEvent> {
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
    AronaGachaLimitConfig.reload()
    DataBaseProvider.start()
    QuartzProvider.start()
    INIT.forEach {
      it.init()
    }
  }

  override fun onDisable() {
    AronaConfig.save()
    AronaNudgeConfig.save()
    AronaGachaConfig.save()
    AronaHentaiConfig.save()
    AronaRepeatConfig.save()
    AronaNotifyConfig.save()
    AronaServiceConfig.save()
    AronaGachaLimitConfig.save()
    AronaServiceManager.saveServiceStatus()
  }

  fun runSuspend(block: suspend () -> Unit) = runBlocking {
    withContext(Arona.coroutineContext) {
      block()
    }
  }
  fun sendExitMessage() {
    if (AronaConfig.sendOfflineMessage) {
      sendMessage(MiraiCode.deserializeMiraiCode(AronaConfig.offlineMessage))
    }
  }

  fun sendMessage(message: String) {
    runBlocking {
      withContext(coroutineContext) {
        AronaConfig.groups.forEach {
          val group =  arona.groups[it] ?: return@forEach
          group.sendMessage(message)
        }
      }
    }
  }

  fun sendMessage(message: MessageChain) {
    runBlocking {
      withContext(coroutineContext) {
        AronaConfig.groups.forEach {
          val group =  arona.groups[it] ?: return@forEach
          group.sendMessage(message)
        }
      }
    }
  }

  fun sendMessageToAdmin(message: String) {
    fun getAdmin(id: Long): NormalMember? {
      AronaConfig.managerGroup.forEach {
        val admin = arona.groups[it]?.get(id)
        if (admin != null) return admin
      }
      return null
    }
    runBlocking {
      withContext(coroutineContext) {
        if (AronaConfig.groups.isEmpty() || AronaConfig.managerGroup.isEmpty()) return@withContext
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

  fun info(message: String?) = logger.info(message)

  fun warning(message: String?) = logger.warning(message)

  fun error(message: String?) = logger.error(message)

  fun verbose(message: String?) = logger.verbose(message)

  fun info(message: () -> String?) = logger.info(message())

  fun warning(message: () -> String?) = logger.warning(message())

  fun verbose(message: () -> String?) = logger.verbose(message())

  fun error(message: () -> String?) = logger.error(message())

}