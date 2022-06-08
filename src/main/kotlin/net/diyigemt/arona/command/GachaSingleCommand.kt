package net.diyigemt.arona.command

import kotlinx.serialization.json.jsonObject
import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.data.GachaData
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
    val result = pikerUp()
    val userId = user.id
    val history = (GachaData.getHistory(userId) ?: 0) + 1
    GachaData.putHistory(userId, history)
    val s = "${resultData2String(result)}\n${history} points"
    val dog = if (result.jsonObject["name"].toString().contains("亚津子")) "恭喜老师,出货了呢" else ""
    subject.sendMessage(MessageUtil.atMessageAndCTRL(user, dog, s))
  }

}