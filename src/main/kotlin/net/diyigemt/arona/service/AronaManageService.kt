package net.diyigemt.arona.service

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaConfig
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.code.MiraiCode
import net.mamoe.mirai.message.data.MessageChainBuilder

interface AronaManageService: AronaService {

  fun checkAdmin(user: User, contact: Contact): Boolean {
    if (!AronaConfig.managerGroup.contains(user.id)) {
      if (AronaConfig.permissionDeniedMessage != "") {
        Arona.runSuspend {
          contact.sendMessage(MiraiCode.deserializeMiraiCode(AronaConfig.permissionDeniedMessage, contact))
        }
      }
      return false
    }
    return true
  }

}