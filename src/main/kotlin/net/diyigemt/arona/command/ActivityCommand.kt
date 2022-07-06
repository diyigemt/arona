package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.util.ActivityUtil
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.MessageUtil
import net.diyigemt.arona.util.TimeUtil.calcTime
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

object ActivityCommand : CompositeCommand(
  Arona,"active", "活动",
  description = "通过bili wiki获取活动列表"
) {

  @SubCommand("en")
  @Description("查看国际服活动")
  suspend fun UserCommandSender.activities() {
    sendEN(subject)
  }

  @SubCommand("jp")
  @Description("查看日服活动")
  suspend fun UserCommandSender.activities_jp() {
    sendJP(subject)
  }

  suspend fun sendEN(subject: Contact) {
    if (!GeneralUtils.checkService(subject)) return
    val enActivity = ActivityUtil.fetchENActivity()
    send(subject, enActivity)
  }

  suspend fun sendJP(subject: Contact) {
    if (!GeneralUtils.checkService(subject)) return
    var jpActivity = ActivityUtil.fetchJPActivity()
    if (jpActivity.first.isEmpty() && jpActivity.second.isEmpty()) {
      subject.sendMessage("biliwiki寄了, 从wikiru拉取...")
    }
    jpActivity = ActivityUtil.fetchJPActivityFromJP()
    if (jpActivity.first.isEmpty() && jpActivity.second.isEmpty()) {
      subject.sendMessage("wikiru也寄了")
      return
    }
    send(subject, jpActivity)
  }

  private suspend fun send(subject: Contact, activities: Pair<List<Activity>, List<Activity>>) {
    subject.sendMessage(ActivityUtil.constructMessage(activities))
  }

}