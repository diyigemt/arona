package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaNotifyConfig
import net.diyigemt.arona.entity.Activity
import net.diyigemt.arona.entity.ServerLocale
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.util.ActivityUtil
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage

object ActivityCommand : SimpleCommand(
  Arona,"active", "活动",
  description = "通过wiki获取活动列表"
), AronaService {

  @Handler
  suspend fun UserCommandSender.activities(source: String?) {
    suspend fun sendByLocale(locale: ServerLocale) {
      when(locale) {
        ServerLocale.JP -> sendJP(subject)
        ServerLocale.GLOBAL -> sendEN(subject)
        ServerLocale.CN -> sendCN(subject)
      }
    }
    val preMatch = ServerLocale.values().firstOrNull { it.commandName == source || it.serverName == source }
    when {
      source.isNullOrBlank() -> sendByLocale(AronaNotifyConfig.defaultActivityCommandServer)
      preMatch == null -> {
        subject.sendMessage("参数不匹配, 是否想要执行:\n" +
          "/活动 日服 # 查询日服活动\n" +
          "/活动 国服 # 查询国服活动\n" +
          "/活动 国际服 # 查询国际服活动")
      }
      else -> {
        sendByLocale(preMatch)
      }
    }
  }

  private suspend fun sendJP(subject: Contact) {
    val jpActivity = ActivityUtil.fetchJPActivity()
    send(subject, jpActivity)
  }

  private suspend fun sendEN(subject: Contact) {
    val enActivity = ActivityUtil.fetchENActivity()
    send(subject, enActivity, ServerLocale.GLOBAL)
  }

  private suspend fun sendCN(subject: Contact) {
    val cnActivity = ActivityUtil.fetchCNActivity()
    send(subject, cnActivity, ServerLocale.CN)
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
    register()
  }
}
