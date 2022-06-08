package net.diyigemt.arona.command

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.data.GachaData
import net.diyigemt.arona.util.GachaUtil.pickUpTwoStar
import net.diyigemt.arona.util.GachaUtil.pikerUp
import net.diyigemt.arona.util.GachaUtil.resultData2String
import net.diyigemt.arona.util.MessageUtil
import net.diyigemt.arona.util.MessageUtil.atAndCTRL
import net.diyigemt.arona.util.MessageUtil.atMessageAndCTRL
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.message.data.At

object GachaMultiCommand : SimpleCommand(
  Arona,"gacha_multi", "十连",
  description = "模拟十连"
) {

  @Handler
  suspend fun UserCommandSender.gacha_multi() {
    val result = Array<JsonElement>(10) { pikerUp() }
    val starMap = result.map { it.jsonObject["star"].toString().toInt() }
    val stars = starMap.reduce { prv, cur -> prv + cur }
    if (stars <= 10) result[9] = pickUpTwoStar()
    val s = result.map { resultData2String(it) }
      .reduceIndexed { index, prv, cur -> if (index == 4) "$prv $cur\n" else "$prv $cur" }
    val pickUpNum = result.filter { it.jsonObject["name"].toString().contains("亚津子") }.size
    val userId = user.id
    val history = (GachaData.getHistory(userId) ?: 0) + 10
    if (pickUpNum != 0) {
      GachaData.saveDog(userId, history)
    }
    GachaData.putHistory(userId, history)
    val ss = "${s}\n${history} points"
    val dog = if (pickUpNum != 0) "恭喜老师,出货了呢" else ""
    subject.sendMessage(atMessageAndCTRL(user, dog, ss))
  }
}