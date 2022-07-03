package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.cache.GachaCache
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.config.AronaGachaLimitConfig
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.gacha.GachaHistoryTable
import net.diyigemt.arona.util.GeneralUtils
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

object GachaConfigCommand : CompositeCommand(
  Arona,"gacha", "抽卡",
  description = "设置发情触发的关键词"
) {

  @SubCommand
  @Description("设置激活的池子")
  suspend fun UserCommandSender.setpool(pool: Int) {
    if (!GeneralUtils.checkService(subject)) return
    if (user is Member && (user as Member).permission != MemberPermission.MEMBER) {
      val targetPool = GachaCache.updatePool(pool)
      if (targetPool == null) {
        subject.sendMessage("没有找到池子")
        return
      }
      subject.sendMessage("池子设置为:${targetPool.name}")
    } else {
      subject.sendMessage("爬, 权限不足")
    }
  }

  @SubCommand
  @Description("重置某一池子的记录")
  suspend fun UserCommandSender.reset(pool: Int = AronaGachaConfig.activePool) {
    if (!GeneralUtils.checkService(subject)) return
    if (user is Member && (user as Member).permission != MemberPermission.MEMBER) {
      query {
        GachaHistoryTable.deleteWhere { (GachaHistoryTable.pool eq pool) and (GachaHistoryTable.group eq (subject as Group).id) }
      }
      AronaGachaLimitConfig.forceUpdate()
      subject.sendMessage("历史记录清除成功")
    } else {
      subject.sendMessage("爬, 权限不足")
    }
  }

}