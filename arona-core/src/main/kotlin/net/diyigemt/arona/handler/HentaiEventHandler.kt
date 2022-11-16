package net.diyigemt.arona.handler

import net.diyigemt.arona.Arona.sendTeacherNameMessage
import net.diyigemt.arona.config.AronaReplyConfig
import net.diyigemt.arona.entity.ReplyMessageMatchCondition
import net.diyigemt.arona.entity.ReplyMessageMatchTree
import net.diyigemt.arona.entity.ReplyMessageMatchType
import net.diyigemt.arona.entity.ReplyMessageType
import net.diyigemt.arona.service.AronaGroupService
import net.diyigemt.arona.service.AronaMessageReactService
import net.diyigemt.arona.service.AronaReactService
import net.diyigemt.arona.service.AronaServiceManager
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.UserOrBot
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder

object HentaiEventHandler : AronaMessageReactService<GroupMessageEvent>, AronaGroupService {

  override suspend fun handle(event: GroupMessageEvent) {
    sendReplyMessage(event.message, event.sender, event.subject, ReplyMessageType.MESSAGE)
  }

  override val eventName: String? = GroupMessageEvent::class.simpleName
  override val id: Int = 9
  override val name: String = "发情"
  override var enable: Boolean = true
  override val description: String = name
}

object NudgeEventHandler : AronaReactService<NudgeEvent>, AronaGroupService {

  override suspend fun handle(event: NudgeEvent) {
    sendReplyMessage(MessageChainBuilder().asMessageChain(), event.from as User, event.subject, ReplyMessageType.NUDGE)
  }
  override fun checkService(event: NudgeEvent): Boolean = AronaServiceManager.checkService(NudgeEventHandler, event.from as User, event.subject) == null
  override val eventName: String? = NudgeEvent::class.simpleName
  override val id: Int = 10
  override val name: String = "摸头回复"
  override val description: String = name
  override var enable: Boolean = true
}

private suspend fun sendReplyMessage(message: MessageChain, sender: User, subject: Contact, type: ReplyMessageType) {
  AronaReplyConfig.rules.filter {
    it.messageType == type
  }.filter {
    test(message, sender, subject, it.rule)
  }.forEach {
    val messageList = it.messages
    val total = messageList.sumOf { m -> m.weight }
    var index = (0 until total).random()
    for (msg in messageList) {
      if (index < msg.weight) {
        when(subject) {
          is Group -> subject.sendTeacherNameMessage(sender as UserOrBot, MessageUtil.at(sender, msg.message))
          else -> subject.sendMessage(msg.message)
        }
        return
      } else {
        index -= msg.weight
      }
    }
  }
}

private fun test(message: MessageChain, sender: UserOrBot, subject: Contact, rule: ReplyMessageMatchTree): Boolean {
  if (rule.type != null) {
    val text = message.contentToString()
    return when (rule.type) {
      ReplyMessageMatchType.PREFIX -> text.startsWith(rule.value ?: "")
      ReplyMessageMatchType.SUFFIX -> text.endsWith(rule.value ?: "")
      ReplyMessageMatchType.CONTAINS -> text.contains(rule.value ?: "")
      ReplyMessageMatchType.ACCURATE -> text == rule.value
      ReplyMessageMatchType.SENDER, ReplyMessageMatchType.GROUP -> when (rule.value) {
        null -> false
        else -> when(rule.type) {
          ReplyMessageMatchType.SENDER -> sender.id == rule.value.toLongOrNull()
          ReplyMessageMatchType.GROUP -> (subject is Group) && (subject.id == rule.value.toLongOrNull())
          else -> false
        }
      }
    }
  }
  return when (rule.condition) {
    null -> false
    ReplyMessageMatchCondition.AND -> if (rule.left == null || rule.right == null) {
      false
    } else {
      test(message, sender, subject, rule.left) && test(message, sender, subject, rule.right)
    }
    ReplyMessageMatchCondition.OR -> if (rule.left == null || rule.right == null) {
      false
    } else {
      test(message, sender, subject, rule.left) || test(message, sender, subject, rule.right)
    }
    ReplyMessageMatchCondition.NOT -> if (rule.left == null) {
      false
    } else {
      !test(message, sender, subject, rule.left)
    }
  }
}