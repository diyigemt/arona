package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.data.GachaData
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.nameCardOrNick

object GachaDogCommand : SimpleCommand(
  Arona,"gacha_dog", "狗叫",
  description = "单抽一次"
) {

  @Handler
  suspend fun UserCommandSender.gacha_dog() {
    val dogCall = GachaData.getDogCall().filter { it.dog != 0 }
    if (dogCall.isEmpty()) {
      subject.sendMessage("还没有老师抽出来哦")
      return
    }
    var ss = "狗叫排行:\n"
    dogCall.map {
      val nick = bot.getGroup(726453107L)!![it.id.value]!!.nameCardOrNick
      "${nick}(${it.id.value}): ${it.dog}抽"
    }.forEachIndexed {
      index, s -> ss += "${index + 1}. $s\n"
    }
    subject.sendMessage(ss)
  }

}