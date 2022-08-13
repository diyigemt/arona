package net.diyigemt.arona.util

import net.diyigemt.arona.command.CallMeCommand
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.name.TeacherName
import net.diyigemt.arona.db.name.TeacherNameTable
import net.mamoe.mirai.contact.*
import org.jetbrains.exposed.sql.and

object GeneralUtils {

  fun checkService(group: Contact?): Boolean = when(group) {
    is Group -> AronaConfig.groups.contains(group.id)
    else -> false
  }

  fun clearExtraQute(s: String): String {
    if (s.replace("\"", "").length + 2 == s.length) {
      return s.replaceFirst("\"", "").substring(0, s.length - 2)
    }
    return s
  }

  fun queryTeacherNameFromDB(contact: Contact, user: UserOrBot): String {
    if (!CallMeCommand.enable) return user.nameCardOrNick
    val name = query {
      TeacherName.find { (TeacherNameTable.group eq contact.id) and (TeacherNameTable.id eq user.id) }.firstOrNull()
    }?.name ?: user.nameCardOrNick
    return if (!name.endsWith("老师") && AronaConfig.endWithSensei) "${name}老师" else name
  }

}