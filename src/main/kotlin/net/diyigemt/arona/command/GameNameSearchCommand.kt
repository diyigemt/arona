package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.name.GameName
import net.diyigemt.arona.db.name.GameNameTable
import net.diyigemt.arona.extension.CommandInterceptor
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.PlainText

object GameNameSearchCommand : SimpleCommand(
  Arona,"game_name_search", "谁是", "谁叫",
  description = "根据游戏名反查群友"
), AronaService {

  @Handler
  suspend fun UserCommandSender.gameName(myName: String) {
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
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }

}