package net.diyigemt.arona.interfaces

import net.diyigemt.arona.config.GlobalConfigProvider
import net.mamoe.mirai.console.command.CommandSender

interface ConfigReader {

  val configPrefix: String

}

/**
 * 拿到无configPrefix前缀的全局单一配置
 */
inline fun <reified T> ConfigReader.getMainConfig(key: String): T = GlobalConfigProvider.get(key)

/**
 * 拿到服务的开关配置, 如果没有则使用全局配置
 */
inline fun <reified T> ConfigReader.getGroupServiceConfig(key: String, group: Long): T = GlobalConfigProvider.getGroup(key, group)

/**
 * 拿到所有开启了该服务的群列表, 如果没有则使用全局配置
 */
fun ConfigReader.getGroupServiceList(key: String): List<Long> = getMainConfig<List<Long>>("groups").filter { getGroupServiceConfig(key, it) }
inline fun <reified T> ConfigReader.getConfig(key: String): T = GlobalConfigProvider.get("$configPrefix.$key")
inline fun <reified T> ConfigReader.getGroupConfig(key: String, group: Long): T = GlobalConfigProvider.getGroup("$configPrefix.$key", group)
inline fun <reified T> ConfigReader.getConfigOrDefault(key: String, default: T): T = GlobalConfigProvider.getOrDefault("$configPrefix.$key", default)
inline fun <reified T> ConfigReader.getConfigOrDefault(key: String, group: Long, default: T): T = GlobalConfigProvider.getGroupOrDefault("$configPrefix.$key", group, default)
fun ConfigReader.setConfig(key: String, value: Any) = GlobalConfigProvider.set("$configPrefix.$key", value)
fun ConfigReader.setGroupConfig(key: String, group: Long, value: Any) = GlobalConfigProvider.setGroup("$configPrefix.$key", group, value)
fun CommandSender.getContactId() = subject?.id ?: 0L