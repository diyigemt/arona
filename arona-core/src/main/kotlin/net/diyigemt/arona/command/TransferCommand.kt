package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.service.AronaService
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.ForwardMessageBuilder

object TransferCommand : SimpleCommand(
  Arona, "transfer", "转发",
  description = "合并转发一段时间内的内容"
), AronaService {

  @Handler
  suspend fun UserCommandSender.transferMessageBuilder() {
    Arona.warning("!@3")
    val builder = ForwardMessageBuilder(subject)
    GlobalEventChannel.subscribeOnce<MessageEvent>() {
      val message = builder.add(it).build()
      subject.sendMessage(message)
    }
  }

  override val id: Int = 15
  override val name: String = "合并转发"
  override var enable: Boolean = true

}