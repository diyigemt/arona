package net.diyigemt.arona.web.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.diyigemt.arona.config.GlobalConfigProvider
import net.diyigemt.arona.web.WebUIService
import net.diyigemt.arona.web.api.v1.message.ServerResponse

/**
 *@Author hjn
 *@Create 2022/11/10
 */
fun Application.configureSecurity() {
  authentication {
    jwt {
      realm = WebUIService.realm
      verifier {
        JWT
          .require(Algorithm.HMAC256(WebUIService.secret))
          .withAudience(WebUIService.jwtAudience)
          .withIssuer(WebUIService.issuer)
          .acceptExpiresAt(60000)
          .build()
      }
      validate { credential ->
        if (credential.payload.audience.contains(WebUIService.jwtAudience)) JWTPrincipal(credential.payload) else null
      }
      challenge { _, _ ->
        if(GlobalConfigProvider.get<Int>("webui.port") == 57920) return@challenge
        val json = Json { encodeDefaults = true }
        val res = json.encodeToString(ServerResponse(401, HttpStatusCode.Unauthorized.description, null as String?))
        call.response.header("Content-Type", "application/json")
        call.respond(HttpStatusCode.Unauthorized, res)
      }
    }
  }
}