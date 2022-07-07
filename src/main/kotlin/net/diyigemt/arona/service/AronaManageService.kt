package net.diyigemt.arona.service

import net.diyigemt.arona.config.AronaConfig
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User

interface AronaManageService: AronaService {

  suspend fun checkAdmin(user: User, contact: Contact): Boolean {
    if (!AronaConfig.managerGroup.contains(user.id)) {
      if (AronaConfig.permissionDeniedMessage != "") {
        contact.sendMessage(AronaConfig.permissionDeniedMessage)
      }
      return false
    }
    return true
  }

}