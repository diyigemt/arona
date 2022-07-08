package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.util.GachaUtil
import net.diyigemt.arona.util.GeneralUtils
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.nameCardOrNick

object GachaHistoryCommand : SimpleCommand(
  Arona,"gacha_history", "历史",
  description = "抽卡历史记录"
), AronaService {

  @Handler
  suspend fun UserCommandSender.gachaHistory() {
    if (!GeneralUtils.checkService(subject)) return
    val history = GachaUtil.getHistoryAll((subject as Group).id)
    if (history.isEmpty()) {
      subject.sendMessage("还没有记录哦")
      return
    }
    var ss = "历史排行:\n"
    history
      .map {
        val nick = (subject as Group)[it.id.value]!!.nameCardOrNick
        val rate = if (it.count3 == 0) 0 else it.points / it.count3
        "${nick}(${it.id.value}): ${it.points}抽/${it.count3}个3星 = $rate"
      }
      .subList(0, if (history.size > 6) 6 else history.size)
      .forEachIndexed {
        index, s -> ss += "${index + 1}. $s\n"
      }
    subject.sendMessage(ss.subSequence(0, ss.length - 1).toString())
  }
  override val id: Int = 7
  override val name: String = "抽卡历史查询"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }
}