package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaNotifyConfig
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ServerLocale
import net.diyigemt.arona.extension.CommandInterceptor
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.util.ActivityUtil
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.message.data.Message

object ActivityCommand : CompositeCommand(
  Arona,"active", "活动",
  description = "通过bili wiki获取活动列表"
), AronaService, CommandInterceptor {

  @SubCommand("en")
  @Description("查看国际服活动")
  suspend fun UserCommandSender.activities() {
    val enActivity = ActivityUtil.fetchENActivity()
    send(subject, enActivity, ServerLocale.GLOBAL)
  }

  @SubCommand("jp")
  @Description("查看日服活动")
  suspend fun UserCommandSender.activitiesJP() {
    val jpActivity = ActivityUtil.fetchJPActivity()
    send(subject, jpActivity)
  }

  @SubCommand("cn")
  @Description("查看国服活动")
  suspend fun UserCommandSender.activitiesCN() {
    subject.sendMessage("国服还没开呢,别急")
  }

  private suspend fun send(subject: Contact, activities: Pair<List<Activity>, List<Activity>>, serverLocale: ServerLocale = ServerLocale.JP) {
    val imageFile = ActivityUtil.createActivityImage(activities, serverLocale)
    val image = subject.uploadImage(imageFile, "png")
    subject.sendMessage(image)
  }

  override val id: Int = 3
  override val name: String = "活动查询"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    registerInterceptor()
    register()
  }

   override val level: Int = 1
   private val ACTIVITY_COMMAND = "${CommandManager.commandPrefix}活动"
   @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
   override fun interceptBeforeCall(message: Message, caller: CommandSender): String? {
     if (message.contentToString() != ACTIVITY_COMMAND) return null
     val overrideSubCommand = if (AronaNotifyConfig.defaultActivityCommandServer == ServerLocale.JP) "jp" else "en"
     overrideDefaultCommand(ACTIVITY_COMMAND, overrideSubCommand, caller)
     return "使用默认/活动配置重新执行指令"
   }
}