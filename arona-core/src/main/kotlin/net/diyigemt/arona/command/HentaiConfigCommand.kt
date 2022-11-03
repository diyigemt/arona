package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaHentaiConfig
import net.diyigemt.arona.entity.NudgeMessage
import net.diyigemt.arona.service.AronaManageService
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.remarkOrNameCardOrNick

object HentaiConfigCommand : CompositeCommand(
  Arona,"hentai", "发情",
  description = "设置发情触发的关键词"
), AronaManageService {

  @SubCommand("adds")
  @Description("添加一条关键词或者监听对象")
  suspend fun UserCommandSender.hentaiAddString(action: String, weight: Int = 1) {
    AronaHentaiConfig.messageList.add(NudgeMessage(action, weight))
    subject.sendMessage("添加成功,回复词:$action, 权重:$weight")
  }

  @SubCommand("add")
  @Description("添加监听对象")
  suspend fun UserCommandSender.hentaiAdd(target: Member) {
    AronaHentaiConfig.listen.add(target.id)
    subject.sendMessage("变态:${target.remarkOrNameCardOrNick}(${target.id}) 添加成功")
  }

  @SubCommand("remove")
  @Description("删除监听对象")
  suspend fun UserCommandSender.hentaiRemove(target: Member) {
    AronaHentaiConfig.listen.removeIf { it == target.id }
    subject.sendMessage("变态:${target.remarkOrNameCardOrNick}(${target.id}) 移除成功")
  }

  override val id: Int = 2
  override val name: String = "发情配置"
  override var enable: Boolean = true

}