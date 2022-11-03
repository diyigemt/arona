package net.diyigemt.arona.service

import net.diyigemt.arona.util.sys.SysStatic

interface AronaService {
  val id: Int
  val name: String
  val description: String
  var enable: Boolean

  // remind to registerService
  fun registerService() {
    AronaServiceManager.register(this)
  }
  fun enableService() {}
  fun disableService() {}
}