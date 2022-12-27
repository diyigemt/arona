package net.diyigemt.arona.interfaces

import net.diyigemt.arona.config.GlobalConfigProvider
import net.mamoe.mirai.console.command.CommandSender

interface ConfigReader {

  val configPrefix: String

}

inline fun <reified T> ConfigReader.getConfig(key: String): T = GlobalConfigProvider.get("$configPrefix.$key")
inline fun <reified T> ConfigReader.getGroupConfig(key: String, group: Long): T = GlobalConfigProvider.getGroup("$configPrefix.$key", group)
inline fun <reified T> ConfigReader.getConfigOrDefault(key: String, default: T): T = GlobalConfigProvider.getOrDefault("$configPrefix.$key", default)
inline fun <reified T> ConfigReader.getConfigOrDefault(key: String, group: Long, default: T): T = GlobalConfigProvider.getGroupOrDefault("$configPrefix.$key", group, default)
fun ConfigReader.setConfig(key: String, value: Any) = GlobalConfigProvider.set("$configPrefix.$key", value)
fun ConfigReader.setGroupConfig(key: String, group: Long, value: Any) = GlobalConfigProvider.setGroup("$configPrefix.$key", group, value)
fun CommandSender.getContactId() = subject?.id ?: 0L