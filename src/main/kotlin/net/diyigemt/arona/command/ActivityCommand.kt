package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

object ActivityCommand : SimpleCommand(
  Arona,"active", "活动",
  description = "通过bili wiki获取活动列表"
) {

//  @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
//  override val prefixOptional: Boolean = true

  @Handler
  suspend fun UserCommandSender.activities() {
    val document = Jsoup.connect("https://wiki.biligame.com/bluearchive/%E9%A6%96%E9%A1%B5").get()
    val activities = document.getElementsByClass("activity")
    val active = mutableListOf<Activity>()
    val pending = mutableListOf<Activity>()
    activities.forEach {
      val startTime = it.attr("data-start").replace("维护后","17:00")
      val endTime = it.attr("data-end").replace("维护前","11:00")
      val parseStart = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(startTime)
      val parseEnd = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(endTime)
      val now = Calendar.getInstance().time
      if (now.after(parseEnd)) return@forEach
      val content = it.getElementsByClass("activity__name").text()
      var level = 1
      if (content.indexOf("倍") != -1) {
        level = 2
      } else if (content.indexOf("总力战") != -1) {
        level = 3
      }
      if (now.before(parseStart)) {
        val calcTime = calcTime(now, parseStart)
        pending.add(Activity(content, level, "${calcTime.first}天${calcTime.second}小时后开始"))
      } else {
        val calcTime = calcTime(now, parseEnd)
        active.add(Activity(content, level, "${calcTime.first}天${calcTime.second}小时后结束"))
      }
    }
    pending.sortByDescending { at -> at.level }
    active.sortByDescending { at -> at.level }
    val activeString = active.map { at -> "${at.content}     ${at.time}\n" }.reduceOrNull { prv, cur -> prv + cur }
    val pendingString = pending.map { at -> "${at.content}     ${at.time}\n" }.reduceOrNull { prv, cur -> prv + cur }
    subject.sendMessage("正在进行:\n${activeString}即将开始:\n${pendingString ?: '无'}")
  }

  private fun calcTime(now: Date, time: Date): Pair<Int, Int> {
    val hour = floor(((time.time - now.time) / 1000 / 60 / 60).toDouble())
    val day = floor(hour / 24).toInt()
    val leftHour = (hour - day * 24).toInt()
    return Pair(day, leftHour)
  }

  data class Activity(
    val content: String,
    val level: Int,
    val time: String
  )
}