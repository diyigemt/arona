package net.diyigemt.arona.web

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.diyigemt.arona.Arona
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.web.plugins.configureRouting
import net.diyigemt.arona.web.plugins.configureSerialization
import net.diyigemt.arona.web.plugins.configureTemplating

object WebUIService: AronaService {

  private lateinit var server: ApplicationEngine

  override fun enableService() {
    Arona.runSuspend {
      server = embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        configureTemplating()
        configureSerialization()
        configureRouting()
      }.start(wait = false)
    }
  }

  override fun disableService() {
    server.stop()
  }

  override val id: Int = 24
  override val name: String = "WebUI"
  override var enable: Boolean = true

  override fun init() {
    registerService()
  }
}