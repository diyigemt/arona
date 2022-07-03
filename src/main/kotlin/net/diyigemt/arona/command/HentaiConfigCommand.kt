package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaHentaiConfig
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.NudgeMessage
import net.diyigemt.arona.util.ActivityUtil
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.MessageUtil
import net.diyigemt.arona.util.TimeUtil.calcTime
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.*
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

object HentaiConfigCommand : CompositeCommand(
  Arona,"hentai", "发情",
  description = "设置发情触发的关键词"
) {

  @SubCommand("adds")
  @Description("添加一条关键词或者监听对象")
  suspend fun UserCommandSender.hentai_add_s(action: String, weight: Int = 1) {
    if (!GeneralUtils.checkService(subject)) return
    if (!(user as Member).isOperator()) {
      subject.sendMessage(MessageUtil.atAndCTRL(user, "爬, 权限不足"))
      return
    }
    AronaHentaiConfig.messageList.add(NudgeMessage(action, weight))
    subject.sendMessage("添加成功,回复词:$action, 权重:$weight")
  }

  @SubCommand("add")
  @Description("添加监听对象")
  suspend fun UserCommandSender.hentai_add(target: Member) {
    if (!GeneralUtils.checkService(subject)) return
    if (!(user as Member).isOperator()) {
      subject.sendMessage(MessageUtil.atAndCTRL(user, "爬, 权限不足"))
      return
    }
    if (!AronaHentaiConfig.enable) {
      subject.sendMessage("插件未启用")
      return
    }
    AronaHentaiConfig.listen.add(target.id)
    subject.sendMessage("变态:${target.remarkOrNameCardOrNick}(${target.id}) 添加成功")
  }

  @SubCommand("remove")
  @Description("删除监听对象")
  suspend fun UserCommandSender.hentai_remove(target: Member) {
    if (!GeneralUtils.checkService(subject)) return
    if (!(user as Member).isOperator()) {
      subject.sendMessage(MessageUtil.atAndCTRL(user, "爬, 权限不足"))
      return
    }
    if (!AronaHentaiConfig.enable) {
      subject.sendMessage("插件未启用")
      return
    }
    AronaHentaiConfig.listen.removeIf { it == target.id }
    subject.sendMessage("变态:${target.remarkOrNameCardOrNick}(${target.id}) 移除成功")
  }

  @SubCommand("enable")
  @Description("启动")
  suspend fun UserCommandSender.hentai_enable() {
    if (!GeneralUtils.checkService(subject)) return
    if (user !is Member) return
    if (!(user as Member).isOperator()) {
      subject.sendMessage(MessageUtil.atAndCTRL(user, "爬, 权限不足"))
      return
    }
    AronaHentaiConfig.enable = true
    subject.sendMessage("变态插件启用成功")
  }

  @SubCommand("disable")
  @Description("退出")
  suspend fun UserCommandSender.hentai_disable() {
    if (!GeneralUtils.checkService(subject)) return
    if (user !is Member) return
    if (!(user as Member).isOperator()) {
      subject.sendMessage(MessageUtil.atAndCTRL(user, "爬, 权限不足"))
      return
    }
    AronaHentaiConfig.enable = false
    subject.sendMessage("变态插件关闭成功")
  }

}