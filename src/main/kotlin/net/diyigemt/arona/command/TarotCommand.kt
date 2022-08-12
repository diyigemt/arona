package net.diyigemt.arona.command

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaTarotConfig
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.tarot.Tarot
import net.diyigemt.arona.db.tarot.TarotRecord
import net.diyigemt.arona.db.tarot.TarotRecordTable
import net.diyigemt.arona.service.AronaService
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.nameCardOrNick
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update
import java.util.Calendar

object TarotCommand : SimpleCommand(
  Arona,"tarot", "塔罗牌",
  description = "抽一张塔罗牌"
), AronaService {
  private const val TarotCount = 22
  @Handler
  suspend fun UserCommandSender.tarot() {
    val group0 = if (subject is Group) subject.id else user.id
    val userId = user.id
    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    // 获取历史记录
    val record = query {
      TarotRecord
        .find { (TarotRecordTable.id eq userId) and (TarotRecordTable.group eq group0) }
        .toList()
    }!!
    if (AronaTarotConfig.dayOne) {
      if (record.isNotEmpty() && record[0].day == today) {
        val tarotRecord = record[0]
        val tarot = query {
          Tarot.findById(tarotRecord.tarot)
        }!!
        send(user, subject, tarot, !tarotRecord.positive)
        return
      }
    }
    val tarotIndex = (1 .. TarotCount).random()
    val tarot0 = query {
      Tarot.findById(tarotIndex)
    }!!
    val positive = (1 .. 1000).random() > 500
    send(user, subject, tarot0, positive)
    if (AronaTarotConfig.dayOne) {
      if (record.isNotEmpty()) {
        query {
          TarotRecordTable.update({ (TarotRecordTable.id eq userId) and (TarotRecordTable.group eq group0) }) {
            it[day] = today
            it[tarot] = tarotIndex
          }
        }
        return
      }
      // 写入数据库
      query {
        TarotRecord.new(userId) {
          this.group = group0
          this.day = today
          this.tarot = tarotIndex
          this.positive = positive
        }
      }
    }
  }

  private suspend fun send(user: User, contact: Contact, tarot: Tarot, positive: Boolean) {
    val res = if (positive) tarot.positive else tarot.negative
    val resName = if (positive) "正位" else "逆位"
    contact.sendMessage("看看${user.nameCardOrNick}抽到了什么:\n${tarot.name}(${resName})\n${res}")
  }

  override val id: Int = 16
  override val name: String = "塔罗牌"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }

}