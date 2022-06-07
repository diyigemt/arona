package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.util.GachaUtil.pikerUp
import net.diyigemt.arona.util.GachaUtil.resultData2String
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.message.data.At

object GachaSingleCommand : SimpleCommand(
  Arona,"gacha_one", "单抽",
  description = "单抽一次"
) {

  @Handler
  suspend fun UserCommandSender.gacha_one() {
    subject.sendMessage(MessageUtil.atAndCTRL(user, resultData2String(pikerUp())))
  }

}