package net.diyigemt.arona

import net.diyigemt.arona.command.ActivityCommand
import net.diyigemt.arona.command.GachaMultiCommand
import net.diyigemt.arona.command.GachaSingleCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.info

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
    info(
      """
            这是一个测试插件, 
            在这里描述插件的功能和用法等.
        """.trimIndent()
    )
    // author 和 info 可以删除.
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
      (contains("老婆") or contains("老公")) {
        if (this.sender.id == 758213389L) {
          this.group.sendMessage(At(this.sender).plus("爬"))
        }
      }
    }
    logger.info { "arona loaded" }
    //配置文件目录 "${dataFolder.absolutePath}/"
  }

  private fun init() {
    ActivityCommand.register()
    GachaSingleCommand.register()
    GachaMultiCommand.register()
  }

}