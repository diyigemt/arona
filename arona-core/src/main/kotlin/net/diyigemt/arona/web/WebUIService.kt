package net.diyigemt.arona.web

import io.ktor.server.application.Application
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.diyigemt.arona.Arona
import net.diyigemt.arona.interfaces.ConfigReader
import net.diyigemt.arona.interfaces.Initialize
import net.diyigemt.arona.interfaces.getConfig
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.web.database.DBOptionService
import net.diyigemt.arona.web.plugins.configureCROS
import net.diyigemt.arona.web.plugins.configureRouting
import net.diyigemt.arona.web.plugins.configureSecurity
import net.diyigemt.arona.web.plugins.configureSerialization
import net.mamoe.mirai.console.command.SimpleCommand
import java.net.ServerSocket
import java.net.Socket
import java.util.*

object WebUIService: SimpleCommand(
  Arona, "token", "生成一个WebUI登录密钥（1小时内有效，程序中途退出依然失效）",
  description = "抽卡历史记录"
), AronaService, Initialize, ConfigReader {

  private lateinit var server: ApplicationEngine
  const val jwtAudience = "http://127.0.0.1/api/v1"
  const val issuer = "http://127.0.0.1/"
  const val realm = "WebUI Access to API 'v1'"
  var secret = generateSecret()

  override fun enableService() {
    val savePort = kotlin.runCatching { getConfig<String>("port").toInt() }.getOrElse {
      it.printStackTrace()
      Arona.error("从数据库获取端口号失败, 使用系统分配的端口")
      return@getOrElse kotlin.runCatching {
        val sp = ServerSocket(0)
        val port = sp.localPort
        sp.close()

        return@runCatching port
      }.getOrElse {
        Arona.error("系统未能提供空闲端口号，WebUI服务启动失败")
        return
      }
    }
    val port = kotlin.runCatching { ServerSocket(savePort).localPort }.getOrElse {
      it.printStackTrace()
      Arona.error("端口: ${savePort}已被占用, 使用系统分配的端口")
      return@getOrElse kotlin.runCatching {
        val sp = ServerSocket(0)
        val port = sp.localPort
        sp.close()

        return@runCatching port
      }.getOrNull()
    } ?: kotlin.run {
      Arona.error("系统未能提供空闲端口号，WebUI启动失败")
      return
    }
    Arona.runSuspend {
      server = embeddedServer(
        Netty,
        //TODO
        port = port,
        host = "127.0.0.1",
        module = Application::webUIModule
      ).start(wait = false)
      Arona.info("WebUI服务在端口${port}上启动")
    }
  }

  @Handler
  fun handler(){
    val res = JWT.create()
      .withAudience(jwtAudience)
      .withIssuer(issuer)
      .withExpiresAt(Date(System.currentTimeMillis() + 60000))
      .sign(Algorithm.HMAC256(secret))

    Arona.info(res)
  }

  override fun disableService() {
    kotlin.runCatching {
      server.stop(timeoutMillis = 1000)
    }
    //unregister()
  }

  override val id: Int = 24
  override val name: String = "WebUI"
  override val description: String = "WebUI服务"
  override var isGlobal: Boolean = true
  override val priority: Int = 10
  override val configPrefix = "webui"

  override fun init() {
    DBOptionService.init()
  }

  private fun generateSecret() : String{
    val dictChars = mutableListOf<Char>().apply { "123456789zxcvbnmasdfghjklqwertyuiop".forEach { this.add(it) } }
    val randomStr = StringBuilder().apply { (1..((10..30).random())).onEach { append(dictChars.random()) } }

    return randomStr.toString()
  }
}

fun Application.webUIModule() {
  configureSecurity()
  configureCROS()
  configureSerialization()
  configureRouting()
}