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
import net.diyigemt.arona.Arona.save
import net.diyigemt.arona.command.*
import net.diyigemt.arona.config.*
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.handler.GroupRepeaterHandler
import net.diyigemt.arona.handler.HentaiEventHandler
import net.diyigemt.arona.handler.NudgeEventHandler
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.quartz.ActivityNotify
import net.diyigemt.arona.quartz.QuartzProvider
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.info
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory

object Arona : KotlinPlugin(
  JvmPluginDescription(
    id = "net.diyigemt.arona",
    name = "arona",
    version = "0.1.0"
  ) {
    author("diyigemt")
  }
) {
  private lateinit var arona: Bot
  private val INIT: List<InitializedFunction> = listOf(ActivityNotify)

  override fun onEnable() {
    init()
    if (DataBaseProvider.isConnected()) {
      GlobalEventChannel.filter {
        it is BotOnlineEvent && it.bot.id == AronaConfig.qq
      }.subscribeOnce<BotOnlineEvent> {
        arona = it.bot
        if (AronaConfig.sendOnlineMessage) {
          sendMessage(AronaConfig.onlineMessage)
        }
      }
      if (AronaNudgeConfig.enable) {
        GlobalEventChannel.subscribeAlways<NudgeEvent>(priority = AronaNudgeConfig.priority) {
          NudgeEventHandler.handle(this)
        }
      }
      GlobalEventChannel.subscribeAlways<GroupMessageEvent> {
        GroupRepeaterHandler.handle(this)
        HentaiEventHandler.handle(this)
      }
      logger.info { "arona loaded" }
    } else error("arona database init failed, arona will not start")
  }

  private fun init() {
    AronaConfig.reload()
    AronaGachaConfig.reload()
    AronaGachaConfig.init()
    AronaNudgeConfig.reload()
    AronaHentaiConfig.reload()
    AronaRepeatConfig.reload()
    AronaNotifyConfig.reload()
    AronaGachaLimitConfig.reload()
    GachaDogCommand.register()
    ActivityCommand.register()
    GachaMultiCommand.register()
    GachaConfigCommand.register()
    GachaSingleCommand.register()
    GachaHistoryCommand.register()
    HentaiConfigCommand.register()
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
    AronaGachaLimitConfig.save()
    if (AronaConfig.sendOfflineMessage) {
      sendMessage(AronaConfig.offlineMessage)
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

  fun info(message: String?) = logger.info(message)

  fun warning(message: String?) = logger.warning(message)

  fun error(message: String?) = logger.error(message)

  fun verbose(message: String?) = logger.verbose(message)

  fun info(message: () -> String?) = logger.info(message())

  fun warning(message: () -> String?) = logger.warning(message())

  fun verbose(message: () -> String?) = logger.verbose(message())

  fun error(message: () -> String?) = logger.error(message())

}