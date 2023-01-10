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
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

object GachaSingleCommand : SimpleCommand(
  Arona, "gacha_one", "单抽",
  description = "单抽一次"
), AronaGroupService, ConfigReader {

  @Handler
  suspend fun MemberCommandSenderOnMessage.gachaOne() {
    val userId = user.id
    val groupId = subject.id
    val checkTime = GachaUtil.checkTime(userId, groupId)
    val teacherName = queryTeacherNameFromDB(subject, user)
    if (checkTime <= 0) {
      subject.sendMessage(MessageUtil.at(user, "${teacherName},石头不够了哦,明天再来抽吧"))
      return
    }
    val result = pickup(getContactId(), 1)[0]
    val stars = result.star
    val history = GachaUtil.getHistory(userId, groupId)
    var star3 = 0
    if (stars == 3) {
      star3 = 1
    }
    val hitPickup = hitPickup(result)
    GachaUtil.updateHistory(userId, groupId, addPoints = 1, addCount3 = star3, dog = hitPickup)
    val s = "${resultData2String(result)}\n${history.points + 1} points"
    val dog = if (hitPickup) "恭喜${teacherName},出货了呢" else ""
    val handler = subject.sendMessage(MessageUtil.atMessageAndCTRL(user, dog, s))
    val revokeTime = getGroupConfig<Int>("revokeTime", getContactId())
    if (revokeTime > 0) {
      MessageUtil.recall(handler, revokeTime * 1000L)
    }
  }

  override val id: Int = 4
  override val name: String = "抽卡单抽"
  override var isGlobal: Boolean = false
  override val configPrefix: String = "gacha"
}