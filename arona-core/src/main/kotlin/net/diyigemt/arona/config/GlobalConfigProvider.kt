package net.diyigemt.arona.config

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
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType


object GlobalConfigProvider: Initialize {
  val CONFIG: MutableMap<String, Any?> = mutableMapOf()

  inline fun <reified T> get(key: String): T = parseValue(CONFIG[key], key)

  inline fun <reified T> get(key: String, kType: KType): T = getInffer<T>(key, kType)

  @OptIn(ExperimentalStdlibApi::class)
  inline fun <reified T> parseValue(value: Any?, key: String): T =
    when (value) {
      is T -> value
      is String -> when (T::class) {
        String::class -> value as T
        Int::class,
        Double::class,
        Float::class -> value.cast()
        else -> MoshiUtil.reflect.adapter<T>(T::class.createType()).fromJson(value)!!
      }
      else -> throw RuntimeException("get config: $key error")
    }

  @OptIn(ExperimentalStdlibApi::class)
  inline fun <reified T> getInffer(key: String, kType: KType): T {
    val value = CONFIG[key] ?: throw RuntimeException("get config: $key error")
    return when(value) {
      is String -> MoshiUtil.reflect.adapter<T>(kType).fromJson(value) ?: throw RuntimeException("get config: $key parse error")
      else -> throw RuntimeException("get config: $key type error")
    }
  }

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
  fun getGroupList(): List<Long> = getBotConfig().map { it.groups }.flatMap { it.toList() }

  /**
   * 拿到botConfig
   */
  fun getBotConfig(): List<BotGroupConfig> = get("bots", List::class.createType(listOf(
    KTypeProjection.invariant(BotGroupConfig::class.starProjectedType)
  )))

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
