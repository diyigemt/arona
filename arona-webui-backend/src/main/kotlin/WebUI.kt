package net.diyigemt.arona

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.diyigemt.arona.plugins.configureRouting
import net.diyigemt.arona.plugins.configureSerialization
import net.diyigemt.arona.plugins.configureTemplating
import net.diyigemt.arona.plugins.event.webuievent.CommitEvent
import net.diyigemt.arona.plugins.event.webuievent.HandShakeEvent
import net.diyigemt.arona.plugins.message.SingleMessage
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.broadcast

object WebUI : KotlinPlugin(JvmPluginDescription.loadFromResource()) {
    override fun PluginComponentStorage.onLoad() {
        launch {
            embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
                configureTemplating()
                configureSerialization()
                configureRouting()
            }.start(wait = true)
        }
    }
    override fun onEnable() {
        logger.info("arona-webui loaded")
        launch {
            delay(5000)
            logger.info("Broadcast test start")
            HandShakeEvent(id).broadcast()
            delay(2000)
            val event = CommitEvent(Json.encodeToString(SingleMessage(id, "hello")))
            val res = event.broadcast().action
            logger.info(res)
        }
    }
}