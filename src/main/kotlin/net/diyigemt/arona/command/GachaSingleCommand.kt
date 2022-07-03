package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.data.GachaData
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.util.GachaUtil
import net.diyigemt.arona.util.GachaUtil.hitPickup
import net.diyigemt.arona.util.GachaUtil.pickup
import net.diyigemt.arona.util.GachaUtil.resultData2String
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender

object GachaSingleCommand : SimpleCommand(
  Arona,"gacha_one", "单抽",
  description = "单抽一次"
) {

  @Handler
  suspend fun UserCommandSender.gacha_one() {
    if (!GeneralUtils.checkService(subject)) return
    val userId = user.id
    val checkTime = GachaUtil.checkTime(userId, 1)
    if (checkTime <= 0) {
      subject.sendMessage(MessageUtil.at(user, "老师,石头不够了哦,明天再来抽吧"))
      return
    }
    val result = pickup()
    val stars = result.star
    val history = GachaData.getHistory(userId)
    var star3 = 0
    if (stars == 3) {
      star3 = 1
    }
    val hitPickup = hitPickup(result)
    GachaData.updateHistory(userId, addPoints = 1, addCount3 = star3, dog = hitPickup)
    val s = "${resultData2String(result)}\n${history.points + 1} points"
    val dog = if (hitPickup) "恭喜老师,出货了呢" else ""
    val handler = subject.sendMessage(MessageUtil.atMessageAndCTRL(user, dog, s))
    if (AronaGachaConfig.revoke) {
      MessageUtil.recall(handler)
    }
  }
}