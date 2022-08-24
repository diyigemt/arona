package net.diyigemt.arona.util

import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.CallMeCommand
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.name.TeacherName
import net.diyigemt.arona.db.name.TeacherNameTable
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.jetbrains.exposed.sql.and
import java.io.File

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
    return if (AronaConfig.endWithSensei.isNotBlank() && !name.endsWith(AronaConfig.endWithSensei)) "${name}${AronaConfig.endWithSensei}" else name
  }

  fun randomInt(bound: Int): Int = (System.currentTimeMillis() % bound).toInt()

  fun randomBoolean(): Boolean = System.currentTimeMillis().toString().let {
    it.substring(it.length - 1).toInt() % 2 == 0
  }

  suspend fun uploadChapterHelper() {
    val imageFile = File(Arona.dataFolderPath() + "/map-cache").listFiles()?.get(0) ?: return
    val g = Arona.arona.groups[1002484182]!!
    val name = imageFile.name
    val res = imageFile.toExternalResource("png")
    val upload = g.uploadImage(res)
    res.closed
    Arona.info(res.md5.toString())
    Arona.info(res.sha1.toString())
    val msg = g.sendMessage(upload)
    Arona.info("$name ${msg.source.originalMessage.serializeToMiraiCode()}")
  }

}