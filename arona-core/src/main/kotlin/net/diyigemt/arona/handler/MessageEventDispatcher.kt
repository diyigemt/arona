package net.diyigemt.arona.handler

import net.diyigemt.arona.Arona
import net.diyigemt.arona.event.ConfigInitSuccessEvent
import net.diyigemt.arona.interfaces.ConfigReader
import net.diyigemt.arona.interfaces.Initialize
import net.diyigemt.arona.service.AronaGroupService
import net.diyigemt.arona.service.AronaMessageReactService
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.PlainText

// 复读
object MessageEventDispatcher:
  AronaMessageReactService<MessageEvent>,
  AronaGroupService,
  ConfigReader, Initialize
{
  override suspend fun handle(event: MessageEvent) {
    val contentString = event.message.firstOrNull { it is PlainText }?.contentToString() ?: return
  }

  override val event = MessageEvent::class

  override val id: Int = 31
  override val name: String = "指令重定向"
  override var isGlobal: Boolean = true
  override val description: String = name
  override val configPrefix = "dispatcher"
  override fun init() {
    Arona.globalEventChannel().filter { it is ConfigInitSuccessEvent }.subscribeOnce<ConfigInitSuccessEvent> {
      
    }
  }
}