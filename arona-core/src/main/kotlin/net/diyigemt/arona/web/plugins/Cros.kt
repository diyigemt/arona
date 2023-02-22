package net.diyigemt.arona.web.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCROS(){
  install(CORS) {
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Patch)
    allowHeader(HttpHeaders.Authorization)
    allowHeader(HttpHeaders.Accept)
    allowHeader(HttpHeaders.AcceptEncoding)
    allowHeader(HttpHeaders.AcceptLanguage)
    allowHeader(HttpHeaders.ContentType)
    allowHeader(HttpHeaders.Cookie)
    allowHeader(HttpHeaders.AccessControlAllowHeaders)
    allowHeader(HttpHeaders.AccessControlAllowCredentials)
    allowHeader(HttpHeaders.AccessControlRequestMethod)
    allowHeader(HttpHeaders.AccessControlAllowOrigin)
    allowHeader(HttpHeaders.CacheControl)
    allowHeader(HttpHeaders.Connection)
    allowHeader(HttpHeaders.Host)
    allowHeader(HttpHeaders.Origin)
    allowHeader(HttpHeaders.Pragma)
    allowHeader(HttpHeaders.Referrer)
    allowHeader("Sec-Fetch-Dest")
    allowHeader("Sec-Fetch-Mode")
    allowHeader("Sec-Fetch-Site")
    allowHeader("arona-file-name")
    allowHeader(HttpHeaders.UserAgent)
    anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
  }
}