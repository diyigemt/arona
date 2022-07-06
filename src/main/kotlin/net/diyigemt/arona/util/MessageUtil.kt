package net.diyigemt.arona.util

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain

object MessageUtil {

  private const val MessageRecallJobKey = "MessageRecall"
  private const val JobDataMessageKey = "message"


  fun at(user: User, msg: MessageChain): MessageChain {
    return At(user).plus(msg)
  }

  fun at(user: User, msg: String): MessageChain {
    return At(user).plus(msg)
  }

  fun atAndCTRL(user: User, msg: MessageChain): MessageChain {
    return At(user).plus("\n").plus(msg)
  }

  fun atAndCTRL(user: User, msg: String): MessageChain {
    return At(user).plus("\n").plus(msg)
  }

  fun atMessageAndCTRL(user: User, inline: String, msg: String): MessageChain {
    return At(user).plus("$inline\n").plus(msg)
  }

  fun recall(target: MessageReceipt<Contact>, delay: Long = 1000 * 10) {
    target.recallIn(delay)
  }

}