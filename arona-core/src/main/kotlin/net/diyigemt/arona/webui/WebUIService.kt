package net.diyigemt.arona.webui

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.diyigemt.arona.Arona
import net.diyigemt.arona.service.AronaService

object WebUIService: AronaService {

  private lateinit var server: ApplicationEngine

  override fun enableService() {
    Arona.runSuspend {
      server = embeddedServer(Netty, port = 8080, host = "127.0.0.1") {

      }.start(wait = true)
    }
  }

  override fun disableService() {
    server.stop()
  }

  override val id: Int = 24
  override val name: String = "webui"
  override var enable: Boolean = true

  override fun init() {
    registerService()
  }
}