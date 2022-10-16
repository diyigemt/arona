package net.diyigemt.arona.web.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import net.diyigemt.arona.Arona

fun Application.configureRouting() {


    routing {
//        get("/") {
//            call.respondText("Hello World!")
//        }


        static("/") {
            file("/", "${Arona.dataFolder.absolutePath}/dist/index.html")
        }

        // Static plugin. Try to access `/static/index.css`
        static("/static") {
//            resources("static")
            files("${Arona.dataFolder.absolutePath}/dist/static")
        }
    }
}
