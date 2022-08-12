package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.name.TeacherName
import net.diyigemt.arona.db.name.TeacherNameTable
import net.diyigemt.arona.service.AronaGroupService
import net.diyigemt.arona.util.GeneralUtils.queryTeacherNameFromDB
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import org.jetbrains.exposed.sql.and

object CallMeCommand : SimpleCommand(
  Arona,"call_me", "叫我",
  description = "给自己自定义昵称"
), AronaGroupService {

  @Handler
  suspend fun MemberCommandSenderOnMessage.callMe(name: String?) {
    if (name == null) {
      var teacherName = queryTeacherNameFromDB(subject, user)
      if (teacherName.endsWith("老师")) teacherName = "${teacherName}老师"
      subject.sendMessage("arona会叫你 $teacherName")
      return
    }
    updateTeacherNameToDB(subject, user, name)
  }

  fun updateTeacherNameToDB(group0: Group, user0: User, name0: String) {
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
  override fun init() {
    registerService()
    register()
  }

}