package net.diyigemt.arona.service

import net.diyigemt.arona.util.sys.SysStatic

interface AronaService {
  val id: Int
  val name: String
  var enable: Boolean

  // remind to registerService
  fun init()

  fun registerService() {
    AronaServiceManager.register(this)
  }
  fun enableService() {}
  fun disableService() {}
}