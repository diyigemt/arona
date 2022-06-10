package net.diyigemt.arona.util

import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain

object MessageUtil {

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

}