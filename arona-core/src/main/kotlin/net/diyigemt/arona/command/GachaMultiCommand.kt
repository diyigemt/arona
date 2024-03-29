package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.interfaces.ConfigReader
import net.diyigemt.arona.interfaces.getContactId
import net.diyigemt.arona.interfaces.getGroupConfig
import net.diyigemt.arona.service.AronaGroupService
import net.diyigemt.arona.util.GachaUtil
import net.diyigemt.arona.util.GachaUtil.hitPickup
import net.diyigemt.arona.util.GachaUtil.pickup
import net.diyigemt.arona.util.GachaUtil.resultData2String
import net.diyigemt.arona.util.GeneralUtils.queryTeacherNameFromDB
import net.diyigemt.arona.util.MessageUtil
import net.diyigemt.arona.util.MessageUtil.atMessageAndCTRL
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

object GachaMultiCommand : SimpleCommand(
  Arona, "gacha_multi", "十连",
  description = "模拟十连"
), AronaGroupService, ConfigReader {

  @Handler
  suspend fun MemberCommandSenderOnMessage.gachaMulti() {
    val userId = user.id
    val checkTime = GachaUtil.checkTime(userId, subject.id)
    val teacherName = queryTeacherNameFromDB(subject, user)
    if (checkTime <= 0) {
      subject.sendMessage(MessageUtil.at(user, "${teacherName},石头不够了哦,明天再来抽吧"))
      return
    }
    val result = pickup(getContactId(), checkTime)
    val starMap = result.map { it.star }
    val stars1 = starMap.filter { it == 1 }.size
    val stars2 = starMap.filter { it == 2 }.size
    val stars3 = starMap.filter { it == 3 }.size
    val s = result.map { resultData2String(it) }
      .reduceIndexed { index, prv, cur -> if (index == 4) "$prv $cur\n" else "$prv $cur" }
    val hitPickup = result.any { hitPickup(it) }
    val history = GachaUtil.getHistory(userId, subject.id)
    GachaUtil.updateHistory(userId, subject.id, addPoints = checkTime, addCount3 = stars3, dog = hitPickup)
    val dog = if (hitPickup) "恭喜${teacherName},出货了呢" else ""
    val sss = "3星:$stars3 2星:$stars2 1星:$stars1 ${history.points + checkTime} points\n${s}"
    val handler = subject.sendMessage(atMessageAndCTRL(user, dog, sss))
    val revokeTime = getGroupConfig<Int>("revokeTime", getContactId())
    if (revokeTime > 0) {
      MessageUtil.recall(handler, revokeTime * 1000L)
    }
  }

  override val id: Int = 5
  override val name: String = "抽卡十连"
  override var isGlobal: Boolean = false
  override val configPrefix: String = "gacha"
}