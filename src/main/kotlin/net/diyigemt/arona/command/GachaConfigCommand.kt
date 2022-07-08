package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.cache.GachaCache
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.config.AronaGachaLimitConfig
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.gacha.GachaHistoryTable
import net.diyigemt.arona.service.AronaManageService
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Group
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

object GachaConfigCommand : CompositeCommand(
  Arona,"gacha", "抽卡",
  description = "设置发情触发的关键词"
), AronaManageService {

  @SubCommand("setpool")
  @Description("设置激活的池子")
  suspend fun UserCommandSender.setPool(pool: Int) {
    val targetPool = GachaCache.updatePool(pool)
    if (targetPool == null) {
      subject.sendMessage("没有找到池子")
      return
    }
    subject.sendMessage("池子设置为:${targetPool.name}")
  }

  @SubCommand
  @Description("重置某一池子的记录")
  suspend fun UserCommandSender.reset(pool: Int = AronaGachaConfig.activePool) {
    query {
      GachaHistoryTable.deleteWhere { (GachaHistoryTable.pool eq pool) and (GachaHistoryTable.group eq (subject as Group).id) }
    }
    AronaGachaLimitConfig.forceUpdate()
    subject.sendMessage("历史记录清除成功")
  }

  override val id: Int = 1
  override val name: String = "抽卡配置"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }

}