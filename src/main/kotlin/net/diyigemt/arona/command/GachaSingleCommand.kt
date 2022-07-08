package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.util.GachaUtil
import net.diyigemt.arona.util.GachaUtil.hitPickup
import net.diyigemt.arona.util.GachaUtil.pickup
import net.diyigemt.arona.util.GachaUtil.resultData2String
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Group

object GachaSingleCommand : SimpleCommand(
  Arona,"gacha_one", "单抽",
  description = "单抽一次"
), AronaService {

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
    val history = GachaUtil.getHistory((subject as Group).id, userId)
    var star3 = 0
    if (stars == 3) {
      star3 = 1
    }
    val hitPickup = hitPickup(result)
    GachaUtil.updateHistory((subject as Group).id, userId, addPoints = 1, addCount3 = star3, dog = hitPickup)
    val s = "${resultData2String(result)}\n${history.points + 1} points"
    val dog = if (hitPickup) "恭喜老师,出货了呢" else ""
    val handler = subject.sendMessage(MessageUtil.atMessageAndCTRL(user, dog, s))
    if (AronaGachaConfig.revoke) {
      MessageUtil.recall(handler, AronaGachaConfig.revokeTime * 1000L)
    }
  }

  override val id: Int = 4
  override val name: String = "抽卡单抽"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }
}