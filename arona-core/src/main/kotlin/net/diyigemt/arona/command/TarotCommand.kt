package net.diyigemt.arona.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaTarotConfig
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.tarot.Tarot
import net.diyigemt.arona.db.tarot.TarotRecord
import net.diyigemt.arona.db.tarot.TarotRecordTable
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.GeneralUtils.queryTeacherNameFromDB
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update
import java.util.*

object TarotCommand : SimpleCommand(
  Arona,"tarot", "塔罗牌",
  description = "抽一张塔罗牌"
), AronaService {
  private const val TarotCount = 22
  const val TarotImageFolder: String = "/tarot" // 塔罗牌图片
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
        send(user, subject, tarot, tarotRecord.positive)
        return
      }
    }
    val tarotIndex = GeneralUtils.randomInt(22) + 1
    val tarot0 = query {
      Tarot.findById(tarotIndex)
    }!!
    Thread.sleep((1..10).random().toLong())
    val positive = GeneralUtils.randomBoolean()
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
    val fileSuffix = if (positive) "up" else "down"
    val teacherName = queryTeacherNameFromDB(contact, user)
    val path = "${TarotImageFolder}/${tarot.id.value}-${fileSuffix}.png"
    val s = "看看${teacherName}抽到了什么:\n${tarot.name}(${resName})\n${res}"
    if (AronaTarotConfig.image) {
      // 加载塔罗牌图片
      kotlin.runCatching {
        val imageFile = GeneralUtils.localImageFile(path)
        if (!imageFile.exists()) {
          GeneralUtils.imageRequest(path, imageFile)
        }
        imageFile
      }.onSuccess {
        val builder = MessageChainBuilder()
        builder.add(s)
        builder.add("\n")
        val resource = it.toExternalResource()
        val image = contact.uploadImage(resource)
        builder.add(image)
        contact.sendMessage(builder.build())
        withContext(Dispatchers.IO) {
          resource.close()
        }
      }.onFailure {
        Arona.warning("下载塔罗牌图片: ${tarot.id.value}-${fileSuffix}.png 失败")
        contact.sendMessage(s)
      }
    } else {
      contact.sendMessage(s)
    }
  }

  override val id: Int = 16
  override val name: String = "塔罗牌"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }

}