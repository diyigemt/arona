package net.diyigemt.arona.config

import com.google.gson.Gson
import net.diyigemt.arona.annotations.ConfigKey
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.system.SystemConfigTableModel
import net.diyigemt.arona.interfaces.Initialize
import kotlin.reflect.full.declaredMemberProperties


object GlobalConfigProvider: Initialize {
  val CONFIG: MutableMap<String, Any?> = mutableMapOf()
  val GsonInstance: Gson = Gson()
  inline fun <reified T> get(key: String): T =
    when (val value = CONFIG[key]) {
      is T -> value
      is String -> GsonInstance.fromJson(value, T::class.java).also {
        CONFIG[key] = it
      }
      else -> throw RuntimeException("get config: $key error")
    }

  inline fun <reified T> getOrDefault(key: String, default: T): T = get(key) ?: default

  inline fun <reified T> getGroup(key: String, group: Long): T = get(concatGroupKey(key, group)) ?: get(key)

  inline fun <reified T> getGroupOrDefault(key: String, group: Long, default: T): T = getOrDefault(concatGroupKey(key, group), default) ?: getOrDefault(key, default)

  fun concatGroupKey(key: String, group: Long) = "$group.$key"

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
