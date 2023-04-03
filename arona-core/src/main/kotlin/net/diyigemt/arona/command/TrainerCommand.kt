package net.diyigemt.arona.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaTrainerConfig
import net.diyigemt.arona.entity.TrainerOverride
import net.diyigemt.arona.service.AronaService
import net.diyigemt.arona.util.GeneralUtils
import net.diyigemt.arona.util.GeneralUtils.toHex
import net.diyigemt.arona.util.other.KWatchChannel
import net.diyigemt.arona.util.other.KWatchEvent
import net.diyigemt.arona.util.other.asWatchChannel
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupTempMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.File
import java.lang.Integer.min

object TrainerCommand : SimpleCommand(
  Arona,"trainer", "攻略",
  description = "主线地图和学生攻略"
), AronaService {
  const val StudentRankFolder: String = "/student_rank"
  const val ChapterMapFolder: String = "/chapter_map"
  const val OtherFolder: String = "/some"
  private const val AutoReadConfigFileName = "trainer_config.yml"
  private lateinit var ConfigFileWatcherChannel: KWatchChannel
  private lateinit var ConfigFile: File
  private var ConfigFileMd5: String = ""
  private val overrideList = mutableListOf<TrainerOverride>()
  @Handler
  suspend fun UserCommandSender.trainer(str: String) {
    if (str == "阿罗娜" || str == "彩奈") {
      subject.sendMessage("阿罗娜已经被老师攻略啦>_<")
      return
    }
    val override = overrideList
      .filter { it.name.contains(str) }
      .firstOrNull { it.name.split(",")
        .any { s -> s.trim() == str }
      } ?: TrainerOverride(TrainerOverride.OverrideType.RAW, str, str)
    val value = override.value
    when (override.type) {
      TrainerOverride.OverrideType.IMAGE -> {
        val file = GeneralUtils.localImageFile(value)
        if (!file.exists()) {
          Arona.warning("处理攻略指令别名: $str 时失败,没有找到对应的文件: $value")
          return
        }
        sendImage(subject, file)
      }
      TrainerOverride.OverrideType.RAW -> {
        val result = GeneralUtils.loadImageOrUpdate(value)
        val list = result.list
        // 没有本地文件
        if (result.file != null) {
          sendImage(subject, result.file)
          return
        }
        // 模糊搜索建议关闭
        if (!AronaTrainerConfig.tipWhenNull) {
          return
        }
        // 如果没有远端搜索建议 说明真的找不到
        if (list.isEmpty()) {
          if (AronaTrainerConfig.tipWhenNull) {
            sendMessage("没有对应信息, 请联系作者添加别名或者在配置文件中指定")
          }
        } else {
          // 无精确匹配结果, 但是有搜索建议, 发送建议
          val hasResponseWaitTime = AronaTrainerConfig.tipResponseWaitTime != 0
          val preMessage = list
            .map { it.name }
            .filterIndexed { index, _ -> index < 4 }
          val message = (if (hasResponseWaitTime) preMessage.mapIndexed { index, it ->
            "${index + 1}. $it"
          } else preMessage.map { "/攻略 $it" }).joinToString("\n")
          val revoke = sendMessage("没有与${str}对应的信息, 是否想要输入:\n$message")
          if (AronaTrainerConfig.tipRevokeTime != 0) {
            revoke?.recallIn(min(350, AronaTrainerConfig.tipRevokeTime) * 1000L)
          }
          fun send(select: Int) {
            if (select > list.size || select < 0) {
              return
            }
            val result0 = GeneralUtils.loadImageOrUpdate(list[select - 1].name)
            if (result0.file != null) {
              Arona.runSuspend {
                sendImage(subject, result0.file)
              }
            }
          }
          if (hasResponseWaitTime) {
            when(subject) {
              is Group -> {
                waitForNextMessage<GroupMessageEvent>(user.id, AronaTrainerConfig.tipResponseWaitTime) { send(it) }
              }
              is Friend -> {
                waitForNextMessage<FriendMessageEvent>(user.id, AronaTrainerConfig.tipResponseWaitTime) { send(it) }
              }
              is Stranger -> {
                waitForNextMessage<GroupTempMessageEvent>(user.id, AronaTrainerConfig.tipResponseWaitTime) { send(it) }
              }
            }
          }
        }
      }
      TrainerOverride.OverrideType.CODE -> {
        sendMessage(override.value.deserializeMiraiCode(subject))
      }
    }
  }

  private inline fun <reified T: MessageEvent> waitForNextMessage(target: Long, wait: Int, crossinline block: (select: Int) -> Unit) {
    Arona.runAsync {
      withTimeout(wait * 1000L) {
        val feedback = it.asFlow().filterIsInstance<T>().filter { it.sender.id == target }.first()
        val messageString = feedback.message.filterIsInstance<PlainText>().firstOrNull()?.toString() ?: "0"
        kotlin.runCatching {
          messageString.toInt()
        }.onSuccess {
          block(it)
        }
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

  // 模糊查询缓存
  private fun rebuildFuzzySearchCache() {
    kotlin.runCatching {
      val read = ConfigFile.readText(Charsets.UTF_8).also {
        if (it.isBlank()) {
          return
        }
      }
      GeneralUtils.md5(read).toHex().also {
        if (it == ConfigFileMd5) {
          return
        } else {
          ConfigFileMd5 = it
        }
      }
      net.mamoe.yamlkt.Yaml.decodeFromString(TrainerFileConfig.serializer(), read)
    }.onFailure { err ->
      Arona.warning("序列化别名配置时失败")
      Arona.warning(err.message)
      err.printStackTrace()
      return
    }.onSuccess {
      overrideList.clear()
      overrideList.addAll(it.override)
    }
    overrideList.addAll(AronaTrainerConfig.override.filter {
      !overrideList.any { already -> already.name == it.name }
    })
    Arona.info("别名配置更新成功")
  }

  @Serializable
  data class TrainerFileConfig(
    val override: List<TrainerOverride>
  )

  override val id: Int = 20
  override val name: String = "地图与学生攻略"
  override var enable: Boolean = true
  override fun init() {
    registerService()
    register()
    overrideList.addAll(AronaTrainerConfig.override)
    // 监视data文件夹下的arona-trainer.yml文件动态添加配置
    ConfigFile = File(Arona.dataFolderPath("/${GeneralUtils.CONFIG_FOLDER}/${AutoReadConfigFileName}"))
    if (!ConfigFile.exists()) {
      ConfigFile.writeText("override: []", Charsets.UTF_8)
    }
    Arona.runSuspend {
      ConfigFileWatcherChannel = ConfigFile.asWatchChannel(KWatchChannel.Mode.SingleFile)
      ConfigFileWatcherChannel.consumeEach {
        when (it.kind) {
          KWatchEvent.Kind.Modified, KWatchEvent.Kind.Initialized -> rebuildFuzzySearchCache()
          else -> {}
        }
      }
    }
  }

  override fun disableService() {
    ConfigFileWatcherChannel.close()
    super.disableService()
  }

}