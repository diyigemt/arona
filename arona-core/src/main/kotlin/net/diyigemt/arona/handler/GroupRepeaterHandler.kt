package net.diyigemt.arona.handler

import net.diyigemt.arona.interfaces.ConfigReader
import net.diyigemt.arona.interfaces.getGroupConfig
import net.diyigemt.arona.service.AronaGroupService
import net.diyigemt.arona.service.AronaMessageReactService
import net.mamoe.mirai.event.events.GroupMessageEvent

// 复读
object GroupRepeaterHandler:
  AronaMessageReactService<GroupMessageEvent>,
  AronaGroupService,
  ConfigReader
{
  private val _last: MutableMap<Long, String> = mutableMapOf()
  private val _lastSender: MutableMap<Long, Long> = mutableMapOf()
  private val _count: MutableMap<Long, Int> = mutableMapOf()
  override suspend fun handle(event: GroupMessageEvent) {
    val now = event.message.serializeToMiraiCode()
    if (now.startsWith("/")) return
    val senderId = event.sender.id
    val group = event.group.id
    val last = _last[group] ?: ""
    val lastSender = _lastSender[group] ?: 0L
    if (now == last && senderId != lastSender) {
      val count = ((_count[group] ?: 0) + 1).also {
        _count[group] = it
      }
      if (count >= getGroupConfig<Int>("times", group)) {
        event.subject.sendMessage(event.message)
        _count[group] = 0
        _last[group] = now
        _lastSender[group] = 0L
      }
    } else {
      _last[group] = now
      _lastSender[group] = senderId
      _count[group] = 1
    }
  }

  override val eventName: String? = GroupMessageEvent::class.simpleName

  override val id: Int = 8
  override val name: String = "复读"
  override var isGlobal: Boolean = false
  override val description: String = name
  override val configPrefix = "repeat"
}