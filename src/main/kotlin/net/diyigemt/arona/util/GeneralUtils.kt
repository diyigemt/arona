package net.diyigemt.arona.util

import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.name.TeacherName
import net.diyigemt.arona.db.name.TeacherNameTable
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.nameCardOrNick
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

  fun queryTeacherNameFromDB(group: Group, user: User): String {
    val name = query {
      TeacherName.find { (TeacherNameTable.group eq group.id) and (TeacherNameTable.id eq user.id) }.firstOrNull()
    }?.name ?: user.nameCardOrNick
    return if (name.endsWith("老师")) name else "${name}老师"
  }

}