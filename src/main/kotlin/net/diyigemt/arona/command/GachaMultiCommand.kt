package net.diyigemt.arona.command

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import net.diyigemt.arona.Arona
import net.diyigemt.arona.util.GachaUtil.pickUpTwoStar
import net.diyigemt.arona.util.GachaUtil.pikerUp
import net.diyigemt.arona.util.GachaUtil.resultData2String
import net.diyigemt.arona.util.MessageUtil.atAndCTRL
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
    val stars = result.map { it.jsonObject["star"].toString().toInt() }
      .reduce { prv, cur -> prv + cur }
    if (stars <= 10) result[9] = pickUpTwoStar()
    val s = result.map { resultData2String(it) }
      .reduceIndexed { index, prv, cur -> if (index == 4) "$prv $cur\n" else "$prv $cur" }
    subject.sendMessage(atAndCTRL(user, s))
  }

}