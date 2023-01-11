package net.diyigemt.arona.config

import com.squareup.moshi.Types
import com.squareup.moshi.adapter
import net.diyigemt.arona.Arona
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.system.SystemConfigTable
import net.diyigemt.arona.db.system.SystemConfigTableModel
import net.diyigemt.arona.entity.BotGroupConfig
import net.diyigemt.arona.event.ConfigInitSuccessEvent
import net.diyigemt.arona.event.BaseDatabaseInitEvent
import net.diyigemt.arona.interfaces.Initialize
import net.diyigemt.arona.util.MoshiUtil
import net.mamoe.mirai.console.util.cast
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.globalEventChannel


object GlobalConfigProvider: Initialize {
  val CONFIG: MutableMap<String, Any?> = mutableMapOf()
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
        List::class -> {
          // 尽量别在T里写List，Map这种东西，这种东西只知道自己有个形参叫啥都不知道，信息都不够支持反射的
          Arona.info(value)
          val type = Types.newParameterizedType(List::class.java, BotGroupConfig::class.java)

          MoshiUtil.reflect.adapter<T>(type).fromJson(value)!!
        }
        else -> {
          MoshiUtil.reflect.adapter(T::class.java).fromJson(value)!!
          // GsonInstance.fromJson(value, T::class.java)
        }
      }
      else -> throw RuntimeException("get config: $key error")
    }

  @Suppress("UNCHECKED_CAST")
  fun <T> get(key: String, clazz: Class<T>): T {
    val value = CONFIG[key] ?: throw RuntimeException("get config: $key error")
    return when {
      value::class.java == clazz -> value as T
      value is String -> {
        MoshiUtil.reflect.adapter(clazz).fromJson(value)!!.also {
          if(it::class.java == Any::class.java) throw RuntimeException("get config: serialization error")
          CONFIG[key] = it
        }
//        GsonInstance.fromJson(value, clazz).also {
//          CONFIG[key] = it
//        }
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
        is Float, is Double, is Int -> value.toString()
        else -> {
          // GsonInstance.toJson(value)
          MoshiUtil.reflect.adapter(Any::class.java).toJson(value)
        }
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
    Arona.globalEventChannel().filter { it is BaseDatabaseInitEvent }.subscribeOnce<BaseDatabaseInitEvent> { _ ->
      // 从数据库读取
      DataBaseProvider.query { _ ->
        CONFIG.forEach { entry ->
          val config = SystemConfigTableModel.find { SystemConfigTable.key eq entry.key }.toList().firstOrNull()
          if (config != null) return@forEach
          val v = entry.value
          val castValue = when (v) {
            is String -> v
            is Float, is Double, is Int -> v.toString()
            else -> {
              // GsonInstance.toJson(v)
              MoshiUtil.reflect.adapter(Any::class.java).toJson(v)
            }
          }
          SystemConfigTableModel.new {
            this.key = entry.key
            this.value = castValue
          }
        }

        SystemConfigTableModel.all().forEach {
          CONFIG[it.key] = it.value
        }
      }
      Arona.runSuspend {
        ConfigInitSuccessEvent().broadcast()
      }
    }
  }
}
