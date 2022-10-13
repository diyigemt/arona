package net.diyigemt.arona.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import net.diyigemt.arona.Arona
import net.diyigemt.arona.WebUI
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.plugin.name

fun Application.configureRouting() {


    routing {
//        get("/") {
//            call.respondText("Hello World!")
//        }


        static("/") {
            file("/", "${WebUI.dataFolder.absolutePath}/dist/index.html")
        }

        // Static plugin. Try to access `/static/index.css`
        static("/static") {
//            resources("static")
            files("${WebUI.dataFolder.absolutePath}/dist/static")
        }
    }
}
