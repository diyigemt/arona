package net.diyigemt.arona.web.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import net.diyigemt.arona.web.api.v1.access.GetRefusedMessage
import net.diyigemt.arona.web.api.v1.account.GetBotAccount
import net.diyigemt.arona.web.api.v1.event.GetOfflineEvent
import net.diyigemt.arona.web.api.v1.event.GetOnlineEvent
import net.diyigemt.arona.web.api.v1.group.GetAdministrators
import net.diyigemt.arona.web.api.v1.group.GetServingGroups

fun Application.configureSerialization() {
    install(ContentNegotiation) {
      json()
    }

    routing {
      route("/api"){
        route("/v1"){
          route("account"){
            //获取机器人登录的QQ
            get("/botAccount"){
              GetBotAccount.worker(this)
            }
          }

          route("/group"){
            //获取机器人服务的群
            get("/servingGroup"){
              GetServingGroups.worker(this)
            }

            //获取管理员列表
            get("/admins"){
              GetAdministrators.worker(this)
            }
          }

          route("/access"){
            //获取权限不足的回复消息
            get("/refusedMessage"){
              GetRefusedMessage.worker(this)
            }
          }

          route("/event"){
            //登录时发送消息
            get("/onlineEvent") {
              GetOnlineEvent.worker(this)
            }

            //退出登录时发送消息
            get("/offlineEvent"){
              GetOfflineEvent.worker(this)
            }
          }
        }
      }
    }
}
