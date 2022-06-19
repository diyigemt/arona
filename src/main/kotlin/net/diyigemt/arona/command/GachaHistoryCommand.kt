package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.data.GachaData
import net.diyigemt.arona.threadpool.RecallTimer
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.nameCardOrNick

object GachaHistoryCommand : SimpleCommand(
  Arona,"gacha_history", "历史",
  description = "抽卡历史记录"
) {

  @Handler
  suspend fun UserCommandSender.gacha_history() {
    val history = GachaData.getHistoryAll()
    if (history.isEmpty()) {
      subject.sendMessage("还没有记录哦")
      return
    }
    var ss = "历史排行:\n"
    history.reversed().map {
      val nick = bot.getGroup(726453107L)!![it.first]!!.nameCardOrNick
      val rate = if (it.third == 0) "0.00" else (it.third.toFloat() / it.second).toString()
      "${nick}(${it.first}): ${it.second}抽/${it.third}个3星 = ${rate}%"
    }.forEachIndexed {
      index, s -> ss += "${index + 1}. $s\n"
    }
    RecallTimer.recall(subject.sendMessage(ss))
  }

}