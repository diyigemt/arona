package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.util.ActivityUtil
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
    val enActivity = ActivityUtil.fetchENActivity()
    send(subject, enActivity)
  }

  @SubCommand("jp")
  @Description("查看日服活动")
  suspend fun UserCommandSender.activities_jp() {
    val jpActivity = ActivityUtil.fetchJPActivity()
    if (jpActivity.first.isEmpty() && jpActivity.second.isEmpty()) {
      subject.sendMessage("日服wiki寄了")
      return
    }
    send(subject, jpActivity)
  }

  suspend fun send(subject: Contact, activities: Pair<List<Activity>, List<Activity>>) {
    val activeString = activities.first.map { at -> "${at.content}     ${at.time}\n" }.reduceOrNull { prv, cur -> prv + cur }
    val pendingString = activities.second.map { at -> "${at.content}     ${at.time}\n" }.reduceOrNull { prv, cur -> prv + cur }
    subject.sendMessage("正在进行:\n${activeString ?: '无'}即将开始:\n${pendingString ?: '无'}")
  }

}