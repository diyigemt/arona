package net.diyigemt.arona.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import net.diyigemt.arona.Arona
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.web.database.DBOptionService
import net.diyigemt.arona.web.plugins.configureRouting
import net.diyigemt.arona.web.plugins.configureSerialization

object WebUIService : AronaService {

  private lateinit var server: ApplicationEngine

  override fun enableService() {
    Arona.runSuspend {
      server = embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        install(CORS) {
          allowMethod(HttpMethod.Options)
          allowMethod(HttpMethod.Put)
          allowMethod(HttpMethod.Delete)
          allowMethod(HttpMethod.Patch)
          allowHeader(HttpHeaders.Authorization)
          allowHeader(HttpHeaders.ContentType)
          allowHeader(HttpHeaders.Cookie)
          allowHeader(HttpHeaders.AccessControlAllowHeaders)
          allowHeader(HttpHeaders.AccessControlAllowCredentials)
          allowHeader(HttpHeaders.AccessControlRequestMethod)
          allowHeader(HttpHeaders.CacheControl)
          allowHeader(HttpHeaders.Connection)
          allowHeader(HttpHeaders.Host)
          allowHeader(HttpHeaders.Origin)
          allowHeader(HttpHeaders.Pragma)
          allowHeader(HttpHeaders.Referrer)
          allowHeader("Sec-Fetch-Dest")
          allowHeader("Sec-Fetch-Mode")
          allowHeader("Sec-Fetch-Site")
          allowHeader(HttpHeaders.UserAgent)
          anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
        }
        configureSerialization()
        configureRouting()
      }.start(wait = false)
    }
  }

  override fun disableService() {
    kotlin.runCatching {
      server.stop()
    }
  }

  override val id: Int = 24
  override val name: String = "WebUI"
  override var enable: Boolean = true

  override fun init() {
    registerService()
    DBOptionService.init()
  }
}