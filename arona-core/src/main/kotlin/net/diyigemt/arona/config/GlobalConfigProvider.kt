package net.diyigemt.arona.config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.annotations.TomlComments
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.diyigemt.arona.advance.NGAImageTranslatePusher
import net.diyigemt.arona.interfaces.Initialize
import net.diyigemt.arona.util.GeneralUtils
import java.io.File

object GlobalConfigProvider: Initialize {
  private const val MAIN_CONFIG_FILE_NAME = "arona.toml"
  private val CONFIG: MutableMap<String, String> = mutableMapOf()
  private val TomlInstance: Toml by lazy {
    Toml(
      inputConfig = TomlInputConfig(
        ignoreUnknownNames = true
      )
    )
  }

  override val priority: Int
    get() = 5

  override fun init() {
    val mainConfig = GeneralUtils.configFileFolder("/$MAIN_CONFIG_FILE_NAME").let {
      val f = File(it)
      if (!f.exists()) {
        val def = AronaMainConfig() // 默认设置
        // 将设置写入
        f.writeText(TomlInstance.encodeToString(AronaMainConfig.serializer(), def), Charsets.UTF_8)
        def
      } else {
        TomlInstance.decodeFromString(AronaMainConfig.serializer(), f.readText(Charsets.UTF_8))
      }
    }

  }
}

@Serializable
data class AronaMainConfig(
  @TomlComments("配置arona会响应哪些机器人收到的事件")
  var bots: MutableList<Long> = mutableListOf(),
  @TomlComments("arona服务的群")
  var groups: MutableList<Long> = mutableListOf(),
  @TomlComments("具有管理员权限的qq号")
  var managerGroup: MutableList<Long> = mutableListOf(),
  @TomlComments("每日检查更新的时间")
  var updateCheckTime: Int = 8,
  @TomlComments("是否自动配置命令的console执行权限")
  var autoGrantPermission: Boolean = true,
  @TomlComments("是否允许arona收集匿名统计信息")
  var sendStatus: Boolean = true,
  @TomlComments("远端操作查询间隔,设置为0表示不开启,单位是小时")
  var remoteCheckInterval: Int = 1,

  @TomlComments("webui相关设置", "webui监听端口")
  @SerialName("webui.port")
  val webuiPort: Int = 8080,

  @TomlComments("nga相关设置", "你自己的nga uid")
  @SerialName("nga.uid")
  var ngaUid: String = "",
  @TomlComments(
    "登录后产生的cookie, 可以从名叫\"ngaPassportCid\"的cookie中获取",
    "具体配置方法看这里 https://github.com/diyigemt/arona/blob/master/doc/using.md#nga-config"
  )
  @SerialName("nga.cid")
  var ngaCid: String = "",
  @TomlComments("扫描周期(单位min)")
  @SerialName("nga.checkInterval")
  var ngaCheckInterval: Int = 30,
  @TomlComments("nga数据源(主站寄了的时候可以换一下),可选\"MAIN\"(主站)和\"SUB\"(备用站)")
  @SerialName("nga.source")
  var ngaSource: NGAImageTranslatePusher.NGASource = NGAImageTranslatePusher.NGASource.SUB,
  @TomlComments("要监听的发送者uid以及nga昵称")
  @SerialName("nga.watch")
  var ngaWatch: MutableMap<String, String> = mutableMapOf(
    "42382305" to "xiwang399",
    "40785736" to "安kuzuha",
    "64124793" to "星泠鑫"
  ),
  @TomlComments("已发送的缓存")
  @SerialName("nga.cache")
  var ngaCache: MutableList<Pair<Int, String>> = mutableListOf()
)