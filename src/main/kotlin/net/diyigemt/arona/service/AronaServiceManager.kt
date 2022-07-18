package net.diyigemt.arona.service

import net.diyigemt.arona.Arona
import net.diyigemt.arona.Arona.reload
import net.diyigemt.arona.advance.AronaUpdateChecker
import net.diyigemt.arona.advance.GroupMessageRecorder
import net.diyigemt.arona.advance.NGAImageTranslatePusher
import net.diyigemt.arona.command.*
import net.diyigemt.arona.config.AronaServiceConfig
import net.diyigemt.arona.handler.GroupRepeaterHandler
import net.diyigemt.arona.handler.HentaiEventHandler
import net.diyigemt.arona.handler.NudgeEventHandler
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.quartz.ActivityNotify
import net.diyigemt.arona.util.GeneralUtils
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
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
    !service.enable -> {
      Arona.runSuspend {
        subject.sendMessage("功能未启用")
      }
      "功能未启用"
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
    GroupRepeaterHandler.init()
    HentaiEventHandler.init()
    NudgeEventHandler.init()
    GroupMessageRecorder.init()
    ActivityNotify.init()
    NGAImageTranslatePusher.init()
    AronaUpdateChecker.init()
    TarotCommand.init()
//    TransferCommand.init()
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