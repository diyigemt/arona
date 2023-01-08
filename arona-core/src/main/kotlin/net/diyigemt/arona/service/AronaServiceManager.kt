package net.diyigemt.arona.service

import net.diyigemt.arona.Arona
import net.diyigemt.arona.event.ConfigInitSuccessEvent
import net.diyigemt.arona.interfaces.ConfigReader
import net.diyigemt.arona.interfaces.Initialize
import net.diyigemt.arona.interfaces.getGroupServiceList
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.ReflectionUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel

object AronaServiceManager: Initialize, ConfigReader {

  override val priority: Int = 5
  override val configPrefix = "service"
  private val MAP: MutableMap<String, AronaService> = mapOf<String, AronaService>().toMutableMap()
  private val REACT_MAP: MutableMap<String, MutableList<AronaReactService<BotEvent>>> = mapOf<String, MutableList<AronaReactService<BotEvent>>>().toMutableMap()

  /**
   * 注册一个服务
   * @param service 服务
   */
  fun register(service: AronaService) {
    val service0 = findServiceByName(service.name)
    if (service0 != null) {
      Arona.error("指令${service.name}重复, 请检查")
    } else {
      registerService(service)
    }
  }

  /**
   * 触发一个事件
   */
  suspend fun emit(event: BotEvent) {
    val eventName = event::class.simpleName
    val service = REACT_MAP[eventName] ?: return
    service.forEach {
      Arona.runSuspend {
        if ((event is MessageEvent) && (checkService(it, event.sender, event.subject) == null)) {
          it.handle(event)
        } else if (it.checkService(event)) {
          it.handle(event)
        }
      }
    }
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

  fun checkService(service: AronaService, user: User, subject: Contact): String? = when {
    !service.isGlobal -> {
      "功能未启用"
    }
    service is AronaGroupService -> when {
      subject !is Group -> {
        Arona.runSuspend {
          subject.sendMessage("该功能仅限群聊使用")
        }
        "该功能仅限群聊使用"
      }
      !GeneralUtils.checkService(subject) -> "非服务群聊"
      else -> null
    }
    // 防止在非服务群聊中也执行非群服务指令
    subject is Group && !GeneralUtils.checkService(subject) -> "非服务群聊"
    (service is AronaManageService) && (!service.checkAdmin(user, subject)) -> {
      "权限不足"
    }
    else -> null
  }

  fun getAllService(): List<AronaService> = MAP.filter { entry -> checkNameIsInt(entry.key) }.values.toList()

  private fun checkNameIsInt(name: String): Boolean = try {
    name.toInt()
    true
  } catch (_: Exception) {
    false
  }

  fun findServiceByName(name: String): AronaService? = if (checkNameIsInt(name)) {
    var serviceName = ""
    MAP.forEach {
      if (it.value.id == name.toInt()) {
        serviceName = it.key
      }
    }
    MAP[serviceName]
  } else {
    MAP[name]
  }

  /**
   * 拿到所有服务的开启与关闭
   *
   * 由默认部分和单独的群部分组成
   *
   * 如果默认开启但是所有群设置为关闭时  自动关闭?  设置为关闭
   *
   * 只要有任意一个群设置为开启则开启
   */
  private fun getServiceOpenList(): List<AronaService> {
    val serviceList = ReflectionUtil.getInterfacePetObjectInstance<AronaService>()
    // 首先拿到所有服务的suffix
    val serviceKeyList = serviceList.map { it.name }
    // 拿到服务单独的开关设置? 没有必要 直接获取所有群的开关设置, 因为群没有特定的开关设置必定为默认设置
    val openServiceIndex = serviceKeyList.map { getGroupServiceList(it).isNotEmpty() }.mapIndexed { index, boolean -> if (boolean) index else -1 }.filter { it != -1 }
    return serviceList.filterIndexed { index, service -> openServiceIndex.contains(index) || service.isGlobal }
  }

  fun saveServiceStatus() {
    //TODO
//    val map = AronaServiceConfig.config
//    MAP.forEach {
//      map[it.value.name] = it.value.enable
//    }
  }

  fun disableAll() {
    //TODO
//    AronaServiceConfig.config.forEach {
//      disable(it.key)
//    }
  }

  private fun findServiceById(id: Int): AronaService? = MAP[id.toString()]

  @Suppress("UNCHECKED_CAST")
  private fun registerService(service: AronaService) {
    MAP[service.name] = service
    MAP[service.id.toString()] = service
    if (service is AronaReactService<*>) {
      var list = REACT_MAP[service.eventName ?: ""]
      if (list == null) {
        list = mutableListOf<AronaReactService<BotEvent>>().also { it.add(service as AronaReactService<BotEvent>) }
        REACT_MAP[service.eventName ?: ""] = list
      } else {
        list.add(service as AronaReactService<BotEvent>)
      }
    }
  }

  private fun unregisterService(service: AronaService) {
    MAP.remove(service.name)
    MAP.remove(service.id.toString())
  }

  override fun init() {
    // 根据数据库配置的服务开启开启服务
    Arona.globalEventChannel().filter { it is ConfigInitSuccessEvent }.subscribeOnce<ConfigInitSuccessEvent> { _ ->
      getServiceOpenList().forEach {
        it.enableService()
      }
    }
  }
}