package net.diyigemt.arona.interfaces

interface Initialize {

  /**
   * 初始化方法调用优先级, 数字越小优先级越高
   * 100: 保留
   * 11-99: 非核心功能
   * 1-10: 核心功能
   * 0: 保留
   */
  val priority: Int
    get() = 100

  fun init()

}