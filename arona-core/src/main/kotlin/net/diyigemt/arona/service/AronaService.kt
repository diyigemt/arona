package net.diyigemt.arona.service

interface AronaService {
  /**
   * 服务的唯一id
   */
  val id: Int

  /**
   * 服务的名称,用于在可交互式配置中给用户标识服务?
   */
  val name: String

  /**
   * 服务描述? 既然这样name还有用吗?
   */
  val description: String

  /**
   * 是否启用  将要弃用
   */
  var enable: Boolean

  // remind to registerService
  fun registerService() {
    AronaServiceManager.register(this)
  }
  fun enableService() {}
  fun disableService() {}
}