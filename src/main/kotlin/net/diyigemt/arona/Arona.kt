/*
 * Copyright 2020-2021 StageGuard.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/diyigemt/arona/blob/master/LICENSE
 */
package net.diyigemt.arona

import net.diyigemt.arona.command.*
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.config.AronaGachaLimitConfig
import net.diyigemt.arona.config.AronaHentaiConfig
import net.diyigemt.arona.config.AronaNudgeConfig
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.handler.GroupRepeaterHandler
import net.diyigemt.arona.handler.HentaiEventHandler
import net.diyigemt.arona.handler.NudgeEventHandler
import net.diyigemt.arona.quartz.QuartzProvider
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
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

  override fun onEnable() {
    init()
//    GlobalEventChannel.subscribeGroupMessages {
//      atBot { it ->
//        val commandAndArg = this.message.filter { message -> message::class == PlainText::class }.map { message -> message.contentToString().trim() }.toMutableList()
//        logger.info(commandAndArg.toString())
//        val command = commandAndArg.removeFirst()
//        if (command == "签到") {
//          this.group.sendMessage(At(this.sender).plus("签到成功! 信用点+20000 清辉石+20"))
//        }
//      }
//    }
    GlobalEventChannel.subscribeAlways<NudgeEvent>(priority = AronaNudgeConfig.priority) {
      NudgeEventHandler.handle(this)
    }
    GlobalEventChannel.subscribeAlways<GroupMessageEvent> {
      GroupRepeaterHandler.handle(this)
      HentaiEventHandler.handle(this)
    }
//    GlobalEventChannel.subscribeAlways<GroupTempMessageEvent> {
//      MessageForwardHandler.handle(this)
//    }
    logger.info { "arona loaded" }
  }

  private fun init() {
    AronaGachaConfig.reload()
    AronaGachaConfig.init()
    AronaNudgeConfig.reload()
    AronaHentaiConfig.reload()
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
  }

  override fun onDisable() {
    AronaNudgeConfig.save()
    AronaHentaiConfig.save()
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