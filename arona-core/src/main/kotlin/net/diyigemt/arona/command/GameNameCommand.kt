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
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.Message

object GameNameCommand : SimpleCommand(
  Arona,"game_name", "游戏名",
  description = "记录游戏名与群名的对应关系"
), AronaService, CommandInterceptor {

  @Handler
  suspend fun UserCommandSender.gameName(myName: String) {
    if (myName.length > 50) {
      subject.sendMessage(MessageUtil.at(user, "太长了, 爬"))
      return
    }
    updateGameNameToDB(user, myName)
    subject.sendMessage("游戏名已记录: $myName")
  }

  private fun updateGameNameToDB(user0: User, name0: String) {
    val userId = user0.id
    DataBaseProvider.query {
      val list = GameName.find { (GameNameTable.id eq userId) }.toList()
      if (list.isEmpty()) {
        GameName.new(userId) {
          name = name0
        }
      } else {
        val update = list[0]
        update.name = name0
      }
    }
  }

  override val id: Int = 21
  override val name: String = "游戏名记录"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }

  override val level: Int = 1
  private val CALL_ME_COMMAND = "${CommandManager.commandPrefix}游戏名"
  override fun interceptBeforeCall(message: Message, caller: CommandSender): String? {
    if (message.contentToString() != CALL_ME_COMMAND) return null
    if (caller !is UserCommandSender) return null
    val subject = caller.subject
    val member = caller.user
    val list = GameName.find { (GameNameTable.id eq member.id) }.toList()
    if (list.isEmpty()) {
      Arona.runSuspend {
        subject.sendMessage("游戏名未设置")
      }
    } else {
      Arona.runSuspend {
        subject.sendMessage("当前游戏名为: ${list[0].name}")
      }
    }
    return ""
  }

}