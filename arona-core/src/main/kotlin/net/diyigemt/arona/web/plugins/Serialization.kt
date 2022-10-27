package net.diyigemt.arona.web.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import net.diyigemt.arona.web.api.v1.Contacts
import net.diyigemt.arona.web.api.v1.commit.CommitManager
import net.diyigemt.arona.web.api.v1.config.GetAronaConfig

fun Application.configureSerialization() {
    install(ContentNegotiation) {
      json()
    }

    routing() {
      route("/api"){
        route("/v1"){
          route("/config"){
            get("/aronaConfig"){
              GetAronaConfig.worker(this)
            }

            post("/commit"){
              CommitManager.worker(this)
            }
          }

          get("/contacts"){
            Contacts.worker(this)
          }
        }
      }
    }
}
