package net.diyigemt.arona.service

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaServiceConfig
import net.diyigemt.arona.interfaces.DefaultCommand
import net.diyigemt.arona.interfaces.Initialize
import net.diyigemt.arona.util.GeneralUtils
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.MessageEvent
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.isAccessible
import kotlin.system.exitProcess

object AronaServiceManager: Initialize {

  override val priority: Int = 0
  private val MAP: MutableMap<String, AronaService> = mapOf<String, AronaService>().toMutableMap()
  private val COMMAND_MAP: MutableMap<String, AronaCommandService> = mapOf<String, AronaCommandService>().toMutableMap()
  private val REACT_MAP: MutableMap<String, AronaReactService<BotEvent>> = mapOf<String, AronaReactService<BotEvent>>().toMutableMap()
  private val MESSAGE_MAP: MutableMap<String, AronaReactService<MessageEvent>> = mapOf<String, AronaReactService<MessageEvent>>().toMutableMap()

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
   * 触发一个事件
   */
  suspend fun emit(event: BotEvent) {
    val eventName = event::class.simpleName
    when (event) {
      is MessageEvent -> {
        MESSAGE_MAP[eventName].let {
          it?.handle(event)
        }
        // 进入自定义指令处理流程
        val rawStringMessage = event.message.toString()
        if (!rawStringMessage.startsWith("/")) {
          return
        }
        val parse = rawStringMessage.replaceFirst("/", "").split(" ").toList()
        val command = parse[0]
        val service = COMMAND_MAP[command] ?: return // 没找着
        // 以@DefaultCommand为注解的方法, 或者第一个public并且第一个参数是它自己监听的事件名称的方法作为指令处理函数
        service::class.declaredFunctions
          .firstOrNull { it.hasAnnotation<DefaultCommand>() } ?:
          service::class.declaredFunctions
            .firstOrNull { it.isAccessible && it.parameters.isNotEmpty() && it.parameters[0].type == event::class.createType() } ?: return
      }
      else -> {
        REACT_MAP[eventName].let {
          it?.handle(event)
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
    service.enable = true
    service.enableService()
    return service
  }

  /**
   * 关闭一个服务
   * @param name 服务的全局名称
   */
  fun disable(name: String): AronaService? {
    val service = findServiceByName(name) ?: return null
    service.enable = false
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
    !service.enable -> {
      Arona.runSuspend {
        subject.sendMessage("功能未启用")
      }
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

  fun saveServiceStatus() {
    val map = AronaServiceConfig.config
    MAP.forEach {
      map[it.value.name] = it.value.enable
    }
  }

  fun disableAll() {
    AronaServiceConfig.config.forEach {
      disable(it.key)
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
    AronaServiceConfig.config.forEach {
      if (it.value) {
        enable(it.key)
      } else {
        disable(it.key)
      }
    }
  }
}