package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.data.GachaData
import net.diyigemt.arona.db.gacha.GachaCharacter
import net.diyigemt.arona.threadpool.RecallTimer
import net.diyigemt.arona.util.GachaUtil
import net.diyigemt.arona.util.GachaUtil.hitPickup
import net.diyigemt.arona.util.GachaUtil.pickup
import net.diyigemt.arona.util.GachaUtil.pickup2
import net.diyigemt.arona.util.GachaUtil.resultData2String
import net.diyigemt.arona.util.MessageUtil
import net.diyigemt.arona.util.MessageUtil.atMessageAndCTRL
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender

object GachaMultiCommand : SimpleCommand(
  Arona,"gacha_multi", "十连",
  description = "模拟十连"
) {

  @Handler
  suspend fun UserCommandSender.gacha_multi() {
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
    val pickUpNum = result.filter { hitPickup(it) }.size
    val history = GachaData.getHistory(userId) ?: Triple(userId, 0, 0)
    val newHistory = Triple(userId, history.second + checkTime, history.third + stars3)
    if (pickUpNum != 0) {
      GachaData.saveDog(userId, newHistory.second)
    }
    GachaData.putHistory(newHistory)
    val dog = if (pickUpNum != 0) "恭喜老师,出货了呢" else ""
    val sss = "3星:$stars3 2星:$stars2 1星:$stars1 ${newHistory.second} points\n${s}"
    RecallTimer.recall(subject.sendMessage(atMessageAndCTRL(user, dog, sss)))
  }
}