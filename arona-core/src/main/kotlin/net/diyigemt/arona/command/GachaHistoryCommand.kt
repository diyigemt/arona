package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.service.AronaGroupService
import net.diyigemt.arona.util.GachaUtil
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.GeneralUtils.queryTeacherNameFromDB
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

@OptIn(ConsoleExperimentalApi::class)
object GachaHistoryCommand : SimpleCommand(
  Arona, "gacha_history", "历史",
  description = "抽卡历史记录"
), AronaGroupService {

  @Handler
  suspend fun MemberCommandSenderOnMessage.gachaHistory() {
    if (!GeneralUtils.checkService(subject)) return
    val history = GachaUtil.getHistoryAll(subject.id)
    if (history.isEmpty()) {
      subject.sendMessage("还没有记录哦")
      return
    }
    var ss = "历史排行:\n"
    history
      .map {
        val teacherName = queryTeacherNameFromDB(subject, subject[it.id.value]!!)
        val rate = if (it.count3 == 0) 0 else it.points / it.count3
        "${teacherName}(${it.id.value}): ${it.points}抽/${it.count3}个3星 = $rate"
      }
      .subList(0, if (history.size > 6) 6 else history.size)
      .forEachIndexed { index, s ->
        ss += "${index + 1}. $s\n"
      }
    subject.sendMessage(ss.subSequence(0, ss.length - 1).toString())
  }

  override val id: Int = 7
  override val name: String = "抽卡历史查询"
  override var enable: Boolean = true
}