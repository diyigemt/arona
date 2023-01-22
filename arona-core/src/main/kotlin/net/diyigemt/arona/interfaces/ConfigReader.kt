package net.diyigemt.arona.interfaces

import net.diyigemt.arona.config.GlobalConfigProvider
import net.mamoe.mirai.console.command.CommandSender
import kotlin.reflect.KType

interface ConfigReader {

  val configPrefix: String

}

/**
 * 拿到无configPrefix前缀的全局单一配置
 */
inline fun <reified T> ConfigReader.getMainConfig(key: String): T = GlobalConfigProvider.get(key)
inline fun <reified T> ConfigReader.getMainConfig(key: String, kType: KType): T = GlobalConfigProvider.get(key, kType)

/**
 * 拿到群特定服务的服务的开关配置, 如果没有则使用全局配置
 */
inline fun <reified T> ConfigReader.getGroupServiceConfig(key: String, group: Long): T = GlobalConfigProvider.getGroup("service.$key", group)

/**
 * 拿到所有开启了该服务的群列表, 如果没有则使用全局配置
 */
fun ConfigReader.getGroupServiceList(key: String): List<Long> = GlobalConfigProvider.getGroupList().filter { getGroupServiceConfig(key, it) }
inline fun <reified T> ConfigReader.getConfig(key: String): T = GlobalConfigProvider.get(checkPrefix(key))
inline fun <reified T> ConfigReader.getGroupConfig(key: String, group: Long): T = GlobalConfigProvider.getGroup(checkPrefix(key), group)
inline fun <reified T> ConfigReader.getConfigOrDefault(key: String, default: T): T = GlobalConfigProvider.getOrDefault(checkPrefix(key), default)
inline fun <reified T> ConfigReader.getGroupConfigOrDefault(key: String, group: Long, default: T): T = GlobalConfigProvider.getGroupOrDefault(checkPrefix(key), group, default)

/**
 * 复杂类型使用
 */
inline fun <reified T> ConfigReader.getConfig(key: String, kType: KType): T = if (configPrefix.isEmpty()) getMainConfig(key, kType) else GlobalConfigProvider.get(checkPrefix(key), kType)
inline fun <reified T> ConfigReader.getGroupConfig(key: String, group: Long, kType: KType): T = GlobalConfigProvider.getGroup(checkPrefix(key), group, kType)

fun ConfigReader.setConfig(key: String, value: Any) = GlobalConfigProvider.set(checkPrefix(key), value)
fun ConfigReader.setGroupConfig(key: String, group: Long, value: Any) = GlobalConfigProvider.setGroup("$configPrefix.$key", group, value)
fun CommandSender.getContactId() = subject?.id ?: 0L

fun ConfigReader.checkPrefix(key: String): String = if (this.configPrefix.isEmpty()) key else "$configPrefix.$key"