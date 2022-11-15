package net.diyigemt.arona.handler

import net.diyigemt.arona.Arona.sendTeacherNameMessage
import net.diyigemt.arona.config.AronaHentaiConfig
import net.diyigemt.arona.entity.ReplyMessageMatchCondition
import net.diyigemt.arona.entity.ReplyMessageMatchTree
import net.diyigemt.arona.entity.ReplyMessageMatchType
import net.diyigemt.arona.service.AronaGroupService
import net.diyigemt.arona.service.AronaMessageReactService
import net.diyigemt.arona.util.MessageUtil
import net.mamoe.mirai.console.util.safeCast
import net.mamoe.mirai.contact.UserOrBot
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain

object HentaiEventHandler : AronaMessageReactService<GroupMessageEvent>, AronaGroupService {

  override suspend fun handle(event: GroupMessageEvent) {
    AronaHentaiConfig.rules.filter {
      test(event.message, event.sender, it.rule)
    }.forEach {
      val messageList = it.messages
      val target = event.sender
      val subject = event.subject
      val total = messageList.sumOf { m -> m.weight }
      var index = (0 until total).random()
      for (msg in messageList) {
        if (index < msg.weight) {
          subject.sendTeacherNameMessage(target as UserOrBot, MessageUtil.at(target, msg.message))
          return
        } else {
          index -= msg.weight
        }
      }
    }
  }

  private fun test(message: MessageChain, sender: UserOrBot, rule: ReplyMessageMatchTree): Boolean {
    if (rule.type != null) {
      val text = message.contentToString()
      return when (rule.type) {
        ReplyMessageMatchType.PREFIX -> text.startsWith(rule.value ?: "")
        ReplyMessageMatchType.SUFFIX -> text.endsWith(rule.value ?: "")
        ReplyMessageMatchType.CONTAINS -> text.contains(rule.value ?: "")
        ReplyMessageMatchType.ACCURATE -> text == rule.value
        ReplyMessageMatchType.TARGET -> when (rule.value) {
          null -> false
          else -> sender.id == (rule.value.safeCast<Long>() ?: 0L)
        }
      }
    }
    return when (rule.condition) {
      null -> false
      ReplyMessageMatchCondition.AND -> if (rule.left == null || rule.right == null) {
        false
      } else {
        test(message, sender, rule.left) && test(message, sender, rule.right)
      }

      ReplyMessageMatchCondition.OR -> if (rule.left == null || rule.right == null) {
        false
      } else {
        test(message, sender, rule.left) || test(message, sender, rule.right)
      }

      ReplyMessageMatchCondition.NOT -> if (rule.left == null) {
        false
      } else {
        !test(message, sender, rule.left)
      }
    }
  }

  override val eventName: String? = GroupMessageEvent::class.simpleName
  override val id: Int = 9
  override val name: String = "发情"
  override var enable: Boolean = true
  override val description: String = name
}