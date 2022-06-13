package net.diyigemt.arona.handler

import net.mamoe.mirai.event.events.GroupTempMessageEvent
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.nextMessage

// 整合消息
object MessageForwardHandler: AronaEventHandler<GroupTempMessageEvent> {
  override suspend fun handle(event: GroupTempMessageEvent) {
    var result = event.message
    if (result.contentToString() != "/转发") return
    val builder = ForwardMessageBuilder(event.subject)
    var targetEvent: GroupTempMessageEvent = event
    event.subject.sendMessage("请输入要整合的内容,输入\"停止\"结束整合")
    var count = 0
    while (result.contentToString() != "停止") {
      builder.add(targetEvent)
      try {
        result = targetEvent.nextMessage(5000) {
          it.subject.sendMessage("第${++count}条消息")
          targetEvent = it
          true
        }
      } catch (_: Exception) {
        val chainBuilder = MessageChainBuilder()
        chainBuilder.add("停止")
        result = chainBuilder.build()
        event.subject.sendMessage("超时了,停止整合")
      }
    }
    event.subject.sendMessage(builder.build())
  }
}