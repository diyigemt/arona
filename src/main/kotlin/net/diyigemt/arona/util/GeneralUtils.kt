package net.diyigemt.arona.util

import net.diyigemt.arona.config.AronaConfig
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group

object GeneralUtils {

  fun checkService(group: Contact?): Boolean = when(group) {
    is Group -> AronaConfig.groups.contains(group.id)
    else -> false
  }

}