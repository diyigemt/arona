package net.diyigemt.arona.service

interface AronaService {
  val id: Int
  val name: String
  var enable: Boolean

  // remind to registerService
  fun init()

  fun registerService() {
    AronaServiceManager.register(this)
  }
  fun enableService() {
    enable = true
  }
  fun disableService() {
    enable = false
  }
}