package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.name.GameName
import net.diyigemt.arona.db.name.GameNameTable
import net.diyigemt.arona.service.AronaService
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.PlainText

object GameNameSearchCommand : SimpleCommand(
  Arona, "game_name_search", "谁是", "谁叫",
  description = "根据游戏名反查群友"
), AronaService {

  @OptIn(ConsoleExperimentalApi::class)
  @Handler
  suspend fun UserCommandSender.gameName(@Name("要查询的游戏名") myName: String) {
    val res = DataBaseProvider.query {
      GameName.find { GameNameTable.name like "%$myName%" }.toList()
    }!!
    if (res.isEmpty()) {
      subject.sendMessage("没有游戏名叫 '$myName' 的群友")
    }
    val builder = MessageChainBuilder()
    builder.add("查询结果:\n")
    if (subject is Group) {
      res.forEach {
        val member = (subject as Group)[it.id.value]
        if (member == null) {
          builder.add("${it.name} (${it.id.value})\n")
        } else {
          builder.add(PlainText("@${member.nameCardOrNick} (${it.name})"))
          builder.add(PlainText("\n"))
        }
      }
      builder.removeLast()
    } else {
      builder.add(res.map {
        return@map "${it.name}(${it.id.value})"
      }.joinToString(","))
    }
    subject.sendMessage(builder.build())
  }

  override val id: Int = 22
  override val name: String = "游戏名反查"
  override var isGlobal: Boolean = false

}