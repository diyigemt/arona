package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.name.TeacherName
import net.diyigemt.arona.db.name.TeacherNameTable
import net.diyigemt.arona.extension.CommandInterceptor
import net.diyigemt.arona.service.AronaGroupService
import net.diyigemt.arona.util.GeneralUtils.queryTeacherNameFromDB
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.Message
import org.jetbrains.exposed.sql.and

object CallMeCommand : SimpleCommand(
  Arona, "call_me", "叫我",
  description = "给自己自定义昵称"
), AronaGroupService, CommandInterceptor {

  @OptIn(ConsoleExperimentalApi::class)
  @Handler
  suspend fun MemberCommandSenderOnMessage.callMe(@Name("昵称") name: String) {
    var teacherName = name
    if (teacherName.length > 20) {
      subject.sendMessage(MessageUtil.at(user, "太长了, 爬"))
      return
    }
    updateTeacherNameToDB(subject, user, teacherName)
    if (!teacherName.endsWith("老师")) teacherName = "${teacherName}老师"
    subject.sendMessage("好的, $teacherName")
  }

  private fun updateTeacherNameToDB(group0: Group, user0: User, name0: String) {
    val groupId = group0.id
    val userId = user0.id
    DataBaseProvider.query {
      val list = TeacherName.find { (TeacherNameTable.group eq groupId) and (TeacherNameTable.id eq userId) }.toList()
      if (list.isEmpty()) {
        TeacherName.new(userId) {
          group = groupId
          name = name0
        }
      } else {
        val update = list[0]
        update.name = name0
      }
    }
  }

  override val id: Int = 18
  override val name: String = "自定义昵称"
  override var enable: Boolean = true

  override val level: Int = 1
  private val CALL_ME_COMMAND = "${CommandManager.commandPrefix}叫我"
  override fun interceptBeforeCall(message: Message, caller: CommandSender): String? {
    if (message.contentToString() != CALL_ME_COMMAND) return null
    if (caller !is MemberCommandSenderOnMessage) return null
    val group = caller.group
    val member = caller.user
    val teacherName = queryTeacherNameFromDB(group, member)
    Arona.runSuspend {
      group.sendMessage(MessageUtil.at(member, "怎么了, $teacherName"))
    }
    return ""
  }

}