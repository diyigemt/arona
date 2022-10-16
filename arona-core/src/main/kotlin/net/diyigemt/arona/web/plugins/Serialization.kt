package net.diyigemt.arona.web.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.diyigemt.arona.web.api.v1.account.GetAccount

fun Application.configureSerialization() {
    install(ContentNegotiation) {
      json()
    }

    routing {
      get("/json/kotlinx-serialization") {
        call.respond(mapOf("hello" to "world"))
      }

      //获取机器人登录的QQ
      get("/api/v1/account"){
        GetAccount.worker(this)
      }
    }
}
