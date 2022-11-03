package net.diyigemt.arona.advance

import net.diyigemt.arona.handler.AronaEventHandler
import net.diyigemt.arona.service.AronaGroupService
import net.mamoe.mirai.event.events.GroupMessageEvent

object GroupMessageRecorder: AronaEventHandler<GroupMessageEvent>, AronaGroupService {
  override suspend fun handle(event: GroupMessageEvent) {

  }

  override val id: Int = 11
  override val name: String = "岁月史书"
  override val description: String = name
  override var enable: Boolean = true
}