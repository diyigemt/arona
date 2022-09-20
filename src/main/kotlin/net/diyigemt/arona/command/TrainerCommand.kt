package net.diyigemt.arona.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaTrainerConfig
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.image.ImageTableModel
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
import org.jetbrains.exposed.sql.selectAll
import java.io.File

object TrainerCommand : SimpleCommand(
  Arona,"trainer", "攻略",
  description = "主线地图和学生攻略"
), AronaService {
  const val StudentRankFolder: String = "/student_rank"
  const val ChapterMapFolder: String = "/chapter_map"
  const val OtherFolder: String = "/some"
  private val FuzzySearch = mutableListOf<String>()
  @Handler
  suspend fun UserCommandSender.trainer(str: String) {
    if (str == "阿罗娜" || str == "彩奈") {
      subject.sendMessage("阿罗娜已经被老师攻略啦>_<")
      return
    }
    val override = AronaTrainerConfig.override.firstOrNull { it.name == str } ?: TrainerOverride(TrainerOverride.OverrideType.RAW, str, str)
    val name = override.name
    val value = override.value
    when (override.type) {
      TrainerOverride.OverrideType.IMAGE -> {
        val file = GeneralUtils.localImageFile(value)
        if (!file.exists()) {
          Arona.warning("处理攻略指令别名: $name 时失败,没有找到对应的文件: $value")
          return
        }
        sendImage(subject, file)
      }
      TrainerOverride.OverrideType.RAW -> {
        val result = GeneralUtils.loadImageOrUpdate(value)
        var list = result.list.map { it.name }
        // 没有本地文件
        if (result.file == null) {
          // 远端也没有搜索建议 或者远端没有回应 对本地进行查找
          if (list.isEmpty()) {
            list = fuzzySearch(value)
          }
          // 如果仍然没有搜索建议 说明真的找不到
          if (list.isEmpty()) {
            if (AronaTrainerConfig.tipWhenNull) {
              sendMessage("没有对应信息, 请联系作者添加别名或者在配置文件中指定")
            }
          } else {
            // 无精确匹配结果, 但是有搜索建议, 发送建议
            sendMessage("没有与${name}对应的信息, 是否想要输入: ${list.mapIndexed {
                index, it -> "${index + 1}. $it"
            }.joinToString("\n")}")
          }
        } else {
          sendImage(subject, result.file)
        }
      }
      TrainerOverride.OverrideType.CODE -> {
        sendMessage(override.value.deserializeMiraiCode(subject))
      }
    }
  }

  fun fuzzySearch(source: String): List<String> {
    val list = GeneralUtils.fuzzySearch(source, FuzzySearch).map { AronaTrainerConfig.override[it.index].name }.toMutableList()
    // 从数据库查找
    list.addAll(DataBaseProvider.query {
      ImageTableModel.all().map { it.name }.toList()
    }!!)
    return list
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
    AronaTrainerConfig.override.forEach {
      FuzzySearch.add(GeneralUtils.toPinyin(it.name))
    }
  }

}