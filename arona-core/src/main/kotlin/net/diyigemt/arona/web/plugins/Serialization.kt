package net.diyigemt.arona.web.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.diyigemt.arona.web.api.v1.Contacts
import net.diyigemt.arona.web.api.v1.PowerSupplyManager
import net.diyigemt.arona.web.api.v1.blockly.BlocklyAPIService
import net.diyigemt.arona.web.api.v1.commit.CommitManager
import net.diyigemt.arona.web.api.v1.config.ConfigService
import net.diyigemt.arona.web.api.v1.data.Data
import net.diyigemt.arona.web.api.v1.file.UploadManager
import net.diyigemt.arona.web.api.v1.mirai.BotsAPI
import net.diyigemt.arona.web.api.v1.reply.Labels
import net.diyigemt.arona.web.api.v1.reply.MessageGroup
import net.diyigemt.arona.web.api.v1.responseMessage

fun Application.configureSerialization() {
  install(ContentNegotiation) {
    json()
  }

  routing {
    authenticate {
      route("/api/v1") {
        get("/config") { ConfigService.worker(this) }

        route("/contacts") {
          get {
            Contacts.worker(this)
          }
          post("/{group}") {
            Contacts.worker(this)
          }
        }

        route("/db") {
          route("/{dataBase}/{table}") {
            get { Data.worker(this) }
            post { Data.worker(this) }
          }
        }

        get("/ping") {
          this.call.respond(responseMessage("pong"))
        }

        route("/psu"){
          post("/{option}"){
            PowerSupplyManager.worker(this)
          }
        }

        route("/blockly") {
          post("/commit") {
            BlocklyAPIService.worker(this)
          }
          get("/commit") {
            BlocklyAPIService.worker(this)
          }
        }

        route("/reply") {
          route("/label") {
            get { Labels.worker(this) }
            post("/{method}") { Labels.worker(this) }
          }

          route("/group") {
            get { MessageGroup.worker(this) }
            post("/{method}") { MessageGroup.worker(this) }
          }
        }

        route("/file") {
          route("/image") {
            get { UploadManager.worker(this) }
            post { UploadManager.worker(this) }
          }
        }

        route("mirai") {
          route("bot") {
            get { BotsAPI.worker(this) }
          }
        }
      }
    }
  }
}