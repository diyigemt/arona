package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.Arona.save
import net.diyigemt.arona.command.cache.GachaCache
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.gacha.GachaHistoryTable
import net.diyigemt.arona.db.gacha.GachaLimitTable
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
    GachaLimitTable.forceUpdate(if (subject is Group) subject.id else null)
    subject.sendMessage("历史记录重置成功")
  }

  @SubCommand("1s")
  @Description("设置1星出货率")
  suspend fun UserCommandSender.s1(rate: Float) {
    AronaGachaConfig.star1Rate = rate
    AronaGachaConfig.save()
    AronaGachaConfig.init()
  }

  @SubCommand("2s")
  @Description("设置2星出货率")
  suspend fun UserCommandSender.s2(rate: Float) {
    AronaGachaConfig.star2Rate = rate
    AronaGachaConfig.init()
  }
  @SubCommand("3s")
  @Description("设置3星出货率")
  suspend fun UserCommandSender.s3(rate: Float) {
    AronaGachaConfig.star3Rate = rate
    AronaGachaConfig.init()
  }

  @SubCommand("p2s")
  @Description("设置2星PickUp出货率")
  suspend fun UserCommandSender.ps2(rate: Float) {
    AronaGachaConfig.star2PickupRate = rate
    AronaGachaConfig.init()
  }

  @SubCommand("p3s")
  @Description("设置3星PickUp出货率")
  suspend fun UserCommandSender.ps3(rate: Float) {
    AronaGachaConfig.star3PickupRate = rate
    AronaGachaConfig.init()
  }

  override val id: Int = 1
  override val name: String = "抽卡配置"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }

}