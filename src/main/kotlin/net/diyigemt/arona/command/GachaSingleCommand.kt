package net.diyigemt.arona.command

import kotlinx.serialization.json.jsonObject
import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.data.GachaData
import net.diyigemt.arona.threadpool.RecallTimer
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
    val stars = result.jsonObject["star"]
    val userId = user.id
    val history = GachaData.getHistory(userId) ?: Triple(userId, 0, 0)
    var star3 = 0
    if (stars.toString().toInt() == 3) {
      star3 = 1
    }
    val newHistory = Triple(userId, history.second + 1, history.third + star3)
    GachaData.putHistory(newHistory)
    val s = "${resultData2String(result)}\n${newHistory.second} points"
    val dog = if (result.jsonObject["name"].toString().contains("初音")) "恭喜老师,出货了呢" else ""
    RecallTimer.recall((subject.sendMessage(MessageUtil.atMessageAndCTRL(user, dog, s))))
  }

}