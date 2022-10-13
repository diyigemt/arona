import net.diyigemt.arona.WebUI
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import java.util.*

@OptIn(ConsoleExperimentalApi::class)
suspend fun main() {
    MiraiConsoleTerminalLoader.startAsDaemon()
    System.setProperty("proxyHost", "127.0.0.1")
    System.setProperty("proxyPort", "7890")

    val pluginInstance = WebUI

    pluginInstance.load() // 主动加载插件, Console 会调用 Dice.onLoad
    pluginInstance.enable() // 主动启用插件, Console 会调用 Dice.onEnable

//  val properties = Properties().apply { File("account.properties").inputStream().use { load(it) } }
    val properties = Properties()
    properties.setProperty("id", "1741557205")
    properties.setProperty("password", "Text1234")

//  val bot = MiraiConsole.addBot(properties.getProperty("id").toLong(), properties.getProperty("password"))
//    .alsoLogin() // 登录一个测试环境的 Bot

    MiraiConsole.job.join()
}