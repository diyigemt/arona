package net.diyigemt.arona

import net.diyigemt.arona.Arona.save
import net.diyigemt.arona.command.*
import net.diyigemt.arona.command.data.GachaData
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.config.AronaHentaiConfig
import net.diyigemt.arona.config.AronaNudgeConfig
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.handler.GroupRepeaterHandler
import net.diyigemt.arona.handler.HentaiEventHandler
import net.diyigemt.arona.handler.MessageForwardHandler
import net.diyigemt.arona.handler.NudgeEventHandler
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupTempMessageEvent
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.info
import javax.swing.GroupLayout.Group
import kotlin.math.log

/**
 * 使用 kotlin 版请把
 * `src/main/resources/META-INF.services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin`
 * 文件内容改成 `org.example.mirai.plugin.PluginMain` 也就是当前主类全类名
 *
 * 使用 kotlin 可以把 java 源集删除不会对项目有影响
 *
 * 在 `settings.gradle.kts` 里改构建的插件名称、依赖库和插件版本
 *
 * 在该示例下的 [JvmPluginDescription] 修改插件名称，id和版本，etc
 *
 * 可以使用 `src/test/kotlin/RunMirai.kt` 在 ide 里直接调试，
 * 不用复制到 mirai-console-loader 或其他启动器中调试
 */

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
    GlobalEventChannel.subscribeGroupMessages {
      atBot { it ->
        val commandAndArg = this.message.filter { message -> message::class == PlainText::class }.map { message -> message.contentToString().trim() }.toMutableList()
        logger.info(commandAndArg.toString())
        val command = commandAndArg.removeFirst()
        if (command == "签到") {
          this.group.sendMessage(At(this.sender).plus("签到成功! 信用点+20000 清辉石+20"))
        }
      }
    }
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
    //配置文件目录 "${dataFolder.absolutePath}/"
  }

  private fun init() {
    GachaData.reload()
    AronaGachaConfig.reload()
    AronaNudgeConfig.reload()
    AronaHentaiConfig.reload()
    GachaDogCommand.register()
    ActivityCommand.register()
    GachaMultiCommand.register()
    GachaResetCommand.register()
    GachaSingleCommand.register()
    GachaHistoryCommand.register()
    HentaiConfigCommand.register()
    DataBaseProvider.init()
  }

  override fun onDisable() {
    GachaData.save()
    AronaNudgeConfig.save()
    AronaHentaiConfig.save()
  }

  fun info(message: String?) = logger.info(message)

  fun error(message: String?) = logger.error(message)

  fun verbose(message: String?) = logger.verbose(message)

  fun info(message: () -> String?) = logger.info(message())

  fun verbose(message: () -> String?) = logger.verbose(message())

  fun error(message: () -> String?) = logger.error(message())

}