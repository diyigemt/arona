package net.diyigemt.arona.web

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import net.diyigemt.arona.Arona
import net.diyigemt.arona.service.AronaManageService
import net.diyigemt.arona.service.AronaService
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import java.util.*

/**
 *@Author hjn
 *@Create 2022/11/8
 */
object LoginService {
  //以下两个变量初始化需要保证ktor先被初始化
//  lateinit var jwtAudience: String
//  lateinit var issuer: String
//  lateinit var secret: String

//  @Handler
//  fun handler(){
//    val res = JWT.create()
//      .withAudience(jwtAudience)
//      .withIssuer(issuer)
//      .withExpiresAt(Date(System.currentTimeMillis() + 60000))
//      .sign(Algorithm.HMAC256(secret))
//
//    Arona.info(res)
//  }

//  override val id: Int = 25
//  override val name: String = "密钥"
//  override var enable: Boolean = true
}