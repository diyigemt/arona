package net.diyigemt.arona.service

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