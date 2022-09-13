package net.diyigemt.arona.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaTrainerConfig
import net.diyigemt.arona.entity.TrainerOverride
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.NetworkUtil
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.File

object TrainerCommand : SimpleCommand(
  Arona,"trainer", "攻略",
  description = "主线地图和学生攻略"
), AronaService {
  const val StudentRankFolder: String = "/student_rank"
  const val ChapterMapFolder: String = "/chapter_map"
  const val OtherFolder: String = "/some"
  @Handler
  suspend fun UserCommandSender.trainer(str: String) {
    if (str == "阿罗娜" || str == "彩奈") {
      subject.sendMessage("阿罗娜已经被老师攻略啦>_<")
      return
    }
    val override = AronaTrainerConfig.override.firstOrNull { it.name == str } ?: TrainerOverride(TrainerOverride.OverrideType.RAW, str, str)
    when (override.type) {
      TrainerOverride.OverrideType.IMAGE -> {
        val file = GeneralUtils.localImageFile(override.value)
        if (!file.exists()) {
          Arona.warning("处理攻略指令别名: ${override.name} 时失败,没有找到对应的文件: ${override.value}")
          return
        }
        sendImage(subject, file)
      }
      TrainerOverride.OverrideType.RAW -> {
        val file = GeneralUtils.loadImageOrUpdate(override.value).let {
          if (it == null) {
            sendMessage("没有对应信息, 请联系作者添加别名或者在配置文件中指定")
            return
          }
          it
        }
        sendImage(subject, file)
      }
      TrainerOverride.OverrideType.CODE -> {
        sendMessage(override.value.deserializeMiraiCode(subject))
      }
    }
  }

  private suspend fun sendImage(contact: Contact, image: File) {
    val resource = image.toExternalResource()
    contact.sendMessage(contact.uploadImage(resource))
    withContext(Dispatchers.IO) {
      resource.close()
    }
  }

  override val id: Int = 20
  override val name: String = "地图与学生攻略"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
  }

}