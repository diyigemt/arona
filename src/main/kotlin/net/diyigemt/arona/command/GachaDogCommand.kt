package net.diyigemt.arona.command

import kotlinx.serialization.json.jsonObject
import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.data.GachaData
import net.diyigemt.arona.util.GachaUtil.pikerUp
import net.diyigemt.arona.util.GachaUtil.resultData2String
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.message.data.At

object GachaDogCommand : SimpleCommand(
  Arona,"gacha_dog", "狗叫",
  description = "单抽一次"
) {

  @Handler
  suspend fun UserCommandSender.gacha_dog() {
    val dogCall = GachaData.getDogCall()
    if (dogCall.isEmpty()) {
      subject.sendMessage("还没有老师抽出来哦")
      return
    }
    var ss = "狗叫排行:\n"
    dogCall.map {
      val nick = bot.getGroup(726453107L)!![it.first]!!.nick
      "${nick}(${it.first}): ${it.second}抽"
    }.forEachIndexed {
      index, s -> ss += "${index + 1}. $s\n"
    }
    subject.sendMessage(ss)
  }

}