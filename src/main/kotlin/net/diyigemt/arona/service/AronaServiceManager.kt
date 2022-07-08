package net.diyigemt.arona.service

import net.diyigemt.arona.Arona
import net.diyigemt.arona.Arona.reload
import net.diyigemt.arona.command.*
import net.diyigemt.arona.config.AronaServiceConfig
import net.diyigemt.arona.interfaces.InitializedFunction
import kotlin.system.exitProcess

object AronaServiceManager: InitializedFunction() {

  private val MAP: MutableMap<String, AronaService> = mapOf<String, AronaService>().toMutableMap()

  /**
   * 注册一个服务
   * @param service 服务
   */
  fun register(service: AronaService) {
    val service0 = findServiceByName(service.name)
    if (service0 != null) {
      Arona.error("指令${service.name}重复, 请检查")
      exitProcess(-1)
    }
    registerService(service)
  }

  /**
   * 启用一个服务
   * @param name 服务的全局名称
   */
  fun enable(name: String): AronaService? {
    val service = findServiceByName(name) ?: return null
    service.enableService()
    return service
  }

  /**
   * 关闭一个服务
   * @param name 服务的全局名称
   */
  fun disable(name: String): AronaService? {
    val service = findServiceByName(name) ?: return null
    service.disableService()
    return service
  }

  /**
   * 启用一个服务
   * @param id 服务的全局id
   */
  fun enable(id: Int): AronaService? {
    return enable(id.toString())
  }

  /**
   * 关闭一个服务
   * @param id 服务的全局id
   */
  fun disable(id: Int): AronaService? {
    return disable(id.toString())
  }

  fun getAllService(): List<AronaService> = MAP.filter { entry -> checkNameIsInt(entry.key) }.values.toList()

  private fun checkNameIsInt(name: String): Boolean = try {
    name.toInt()
    true
  } catch (_: Exception) {
    false
  }

  fun findServiceByName(name: String): AronaService? = MAP[name]

  fun saveServiceStatus() {
    val map = AronaServiceConfig.config
    MAP.forEach {
      map[it.value.name] = it.value.enable
    }
  }

  private fun findServiceById(id: Int): AronaService? = MAP[id.toString()]

  private fun registerService(service: AronaService) {
    MAP[service.name] = service
    MAP[service.id.toString()] = service
  }

  private fun unregisterService(service: AronaService) {
    MAP.remove(service.name)
    MAP.remove(service.id.toString())
  }

  override fun init() {
    AronaConfigCommand.init()
    GachaConfigCommand.init()
    HentaiConfigCommand.init()
    ActivityCommand.init()
    GachaSingleCommand.init()
    GachaMultiCommand.init()
    GachaDogCommand.init()
    GachaHistoryCommand.init()
    AronaServiceConfig.reload()
    AronaServiceConfig.config.forEach {
      if (it.value) {
        enable(it.key)
      } else {
        disable(it.key)
      }
    }
  }
}