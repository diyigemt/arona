package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.data.GachaData
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission

object GachaResetCommand : SimpleCommand(
  Arona,"gacha_reset", "清除抽卡记录",
  description = "管理员清除抽卡记录"
) {

  @Handler
  suspend fun UserCommandSender.gacha_reset() {
    if (user is Member && (user as Member).permission != MemberPermission.MEMBER) {
      GachaData.history.clear()
      GachaData.dog.clear()
      subject.sendMessage("历史记录清除成功")
    } else {
      subject.sendMessage("爬, 权限不足")
    }
  }

}