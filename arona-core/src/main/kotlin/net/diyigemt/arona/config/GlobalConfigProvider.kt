package net.diyigemt.arona.config

import com.google.gson.Gson
import net.diyigemt.arona.annotations.ConfigKey
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.system.SystemConfigTable
import net.diyigemt.arona.db.system.SystemConfigTableModel
import net.diyigemt.arona.interfaces.Initialize
import net.mamoe.mirai.console.util.cast
import kotlin.reflect.full.declaredMemberProperties


object GlobalConfigProvider: Initialize {
  val CONFIG: MutableMap<String, Any?> = mutableMapOf()
  val GsonInstance: Gson = Gson()
  inline fun <reified T> get(key: String): T = parseValue(CONFIG[key], key)

  /**
   * 123456.service.notify false
   * 789456.service.notify false
   * service.notify true
   */
  inline fun <reified T> getPrefix(prefix: String): List<T> =
    CONFIG
      .filter { pair -> pair.key.startsWith(prefix) }
      .map { pair -> parseValue(pair.value, pair.key) }

  /**
   * 获取所有以prefix开头的配置项的key
   */
  fun getPrefixKey(prefix: String) = CONFIG.map { pair -> pair.key }.filter { key -> key.startsWith(prefix) }

  inline fun <reified T> parseValue(value: Any?, key: String): T =
    when (value) {
      is T -> value
      is String -> when (T::class) {
        String::class -> value as T
        Int::class,
        Double::class,
        Float::class -> value.cast()
        else -> GsonInstance.fromJson(value, T::class.java)
      }
      else -> throw RuntimeException("get config: $key error")
    }

  @Suppress("UNCHECKED_CAST")
  fun <T> get(key: String, clazz: Class<T>): T {
    val value = CONFIG[key] ?: throw RuntimeException("get config: $key error")
    return when {
      value::class.java == clazz -> value as T
      value is String -> GsonInstance.fromJson(value, clazz).also {
        CONFIG[key] = it
      }
      else -> throw RuntimeException("get config: $key error")
    }
  }

  fun <T> getOrDefault(key: String, clazz: Class<T>, default: T): T = get(key, clazz) ?: default

  inline fun <reified T> getOrDefault(key: String, default: T): T = get(key) ?: default

  inline fun <reified T> getGroup(key: String, group: Long): T = get(concatGroupKey(key, group)) ?: get(key)

  inline fun <reified T> getGroupOrDefault(key: String, group: Long, default: T): T = getOrDefault(concatGroupKey(key, group), default) ?: getOrDefault(key, default)

  /**
   * 设置一个该群特有的配置项
   */
  fun setGroup(key: String, group: Long, value: Any) {
    set(concatGroupKey(key, group), value)
  }

  /**
   * 更新/创建一个配置项
   */
  fun set(key: String, value: Any) {
    CONFIG[key] = value
    DataBaseProvider.query { _ ->
      val config = SystemConfigTableModel.find { SystemConfigTable.key eq key }.toList().firstOrNull()
      val castValue = when (value) {
        is String -> value
        is Float, is Double, is Int -> value.cast()
        else -> GsonInstance.toJson(value)
      }
      if (config == null) {
        SystemConfigTableModel.new {
          this.key = key
          this.value = castValue
        }
      } else {
        config.value = castValue
      }
    }
  }

  /**
   * 获取该群特有的配置项前缀
   */
  fun concatGroupKey(key: String, group: Long) = "$group.$key"

  /**
   * 拿到服务的群id列表
   */
  fun getGroupList(): List<Long> = get("groups")

  override val priority: Int
    get() = 5

  override fun init() {
    // 从主配置文件读取
    AronaConfig::class.declaredMemberProperties.forEach { property ->
      val key = property.annotations.firstOrNull {
        it is ConfigKey
      }.let {
        if (it == null) {
          property.name
        } else {
          (it as ConfigKey).value
        }
      }
      val value = property.getter.call()
      CONFIG[key] = value
    }
    // 从数据库读取
    DataBaseProvider.query { _ ->
      SystemConfigTableModel.all().forEach {
        CONFIG[it.key] = it.value
      }
    }
  }
}
