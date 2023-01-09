package net.diyigemt.arona.service

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.config.GlobalConfigProvider
import net.diyigemt.arona.interfaces.ConfigReader
import net.diyigemt.arona.interfaces.getMainConfig
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.code.MiraiCode

interface AronaManageService: AronaService {

  fun checkAdmin(user: User, contact: Contact): Boolean {
    val managerGroup = GlobalConfigProvider.get<List<Long>>("managerGroup")
    //TODO
//    if (!AronaConfig.managerGroup.contains(user.id)) {
//      if (AronaConfig.permissionDeniedMessage != "") {
//        Arona.runSuspend {
//          contact.sendMessage(MiraiCode.deserializeMiraiCode(AronaConfig.permissionDeniedMessage, contact))
//        }
//      }
//      return false
//    }
    return true
  }

}