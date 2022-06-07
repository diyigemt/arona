package org.example.mirai.plugin

import net.diyigemt.arona.Arona
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

@OptIn(ConsoleExperimentalApi::class)
suspend fun main() {
    MiraiConsoleTerminalLoader.startAsDaemon()

    Arona.load()
    Arona.enable()

    val bot = MiraiConsole.addBot(2575966472, "1355247243qwe") {
        fileBasedDeviceInfo()
    }.alsoLogin()

    MiraiConsole.job.join()
}