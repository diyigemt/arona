package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.db.gacha.GachaCharacter
import net.diyigemt.arona.util.GachaUtil
import net.diyigemt.arona.util.GachaUtil.hitPickup
import net.diyigemt.arona.util.GachaUtil.pickup
import net.diyigemt.arona.util.GachaUtil.pickup2
import net.diyigemt.arona.util.GachaUtil.resultData2String
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.MessageUtil
import net.diyigemt.arona.util.MessageUtil.atMessageAndCTRL
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Group

object GachaMultiCommand : SimpleCommand(
  Arona,"gacha_multi", "十连",
  description = "模拟十连"
) {

  @Handler
  suspend fun UserCommandSender.gachaMulti() {
    if (!GeneralUtils.checkService(subject)) return
    val userId = user.id
    val checkTime = GachaUtil.checkTime(userId)
    if (checkTime <= 0) {
      subject.sendMessage(MessageUtil.at(user, "老师,石头不够了哦,明天再来抽吧"))
      return
    }
    val result = Array<GachaCharacter>(checkTime) { pickup() }
    val starMap = result.map { it.star }
    val stars = starMap.reduce { prv, cur -> prv + cur }
    var stars1 = starMap.filter { it == 1 }.size
    var stars2 = starMap.filter { it == 2 }.size
    val stars3 = starMap.filter { it == 3 }.size
    if (stars <= 10 && checkTime == 10) {
      result[9] = pickup2()
      stars1--
      stars2++
    }
    val s = result.map { resultData2String(it) }
      .reduceIndexed { index, prv, cur -> if (index == 4) "$prv $cur\n" else "$prv $cur" }
    val hitPickup = result.any { hitPickup(it) }
    val history = GachaUtil.getHistory((subject as Group).id, userId)
    GachaUtil.updateHistory((subject as Group).id, userId, addPoints = checkTime, addCount3 = stars3, dog = hitPickup)
    val dog = if (hitPickup) "恭喜老师,出货了呢" else ""
    val sss = "3星:$stars3 2星:$stars2 1星:$stars1 ${history.points + checkTime} points\n${s}"
    val handler = subject.sendMessage(atMessageAndCTRL(user, dog, sss))
    if (AronaGachaConfig.revoke) {
      MessageUtil.recall(handler, AronaGachaConfig.revokeTime * 1000L)
    }
  }
}