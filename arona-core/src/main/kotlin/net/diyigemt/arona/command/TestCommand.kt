package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.service.AronaGroupService
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

object TestCommand: SimpleCommand(
  Arona, "arona_test_01", description = "测试指令转发"
), AronaGroupService {
  @Handler
  suspend fun MemberCommandSenderOnMessage.arona_test_01() {
    subject.sendMessage("测试")
  }

  override val id: Int = 30
  override val name: String = "指令转发"
}