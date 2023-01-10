package net.diyigemt.arona.service

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.GlobalConfigProvider
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.code.MiraiCode

interface AronaManageService: AronaService {

  fun checkAdmin(user: User, contact: Contact): Boolean {
    val managerGroup = GlobalConfigProvider.get<List<Long>>("managerGroup")
    if (!managerGroup.contains(user.id)) {
      val permissionDeniedMessage = GlobalConfigProvider.getGroup<String>("permissionDeniedMessage", contact.id)
      if (permissionDeniedMessage != "") {
        Arona.runSuspend {
          contact.sendMessage(MiraiCode.deserializeMiraiCode(permissionDeniedMessage, contact))
        }
      }
      return false
    }
    return true
  }

}