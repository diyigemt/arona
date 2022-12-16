package net.diyigemt.arona.config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.annotations.TomlComments
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
    val mainConfigFile = GeneralUtils.configFileFolder("/$MAIN_CONFIG_FILE_NAME").let {
      val f = File(it);
      if (!f.exists()) {
        AronaMainConfig() // 默认设置
      } else {
        TomlInstance.decodeFromString(AronaMainConfig.serializer(), f.readText(Charsets.UTF_8))
      }
    }
    // 将设置写入

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
  @TomlComments("当不具有管理员的用户尝试执行需要管理权限的指令时的回复消息")
  var permissionDeniedMessage: String = "",
  @TomlComments("arona上线消息,留空表示不发送")
  var onlineMessage: String = "",
  @TomlComments("arona下线消息,留空表示不发送")
  var offlineMessage: String = "",
  @TomlComments("每日检查更新的时间")
  var updateCheckTime: Int = 8,
  @TomlComments("称呼带上的后缀,默认是\"老师\"")
  var endWithSensei: String = "老师",
  @TomlComments("是否自动配置命令的console执行权限")
  var autoGrantPermission: Boolean = true,
  @TomlComments("是否允许arona收集匿名统计信息")
  var sendStatus: Boolean = true,
  @TomlComments("远端操作查询间隔,设置为0表示不开启,单位是小时")
  var remoteCheckInterval: Int = 1,
  @TomlComments("webui相关设置")
  val webui: WebUIConfig = WebUIConfig(),
  @TomlComments("nga相关设置")
  var nga: NGAConfig = NGAConfig()
)

@Serializable
data class WebUIConfig(
  @TomlComments("WebUI监听端口")
  var port: Int = 8080
)

@Serializable
data class NGAConfig(
  @TomlComments("你自己的nga uid")
  var uid: String = "",
  @TomlComments(
    "登录后产生的cookie, 可以从名叫\"ngaPassportCid\"的cookie中获取",
    "具体配置方法看这里 https://github.com/diyigemt/arona/blob/master/doc/using.md#nga-config"
  )
  var cid: String = "",
  @TomlComments("扫描周期(单位min)")
  var checkInterval: Int = 30,
  @TomlComments("数据源(主站寄了的时候可以换一下),可选\"MAIN\"(主站)和\"SUB\"(备用站)")
  var source: NGAImageTranslatePusher.NGASource = NGAImageTranslatePusher.NGASource.SUB,
  @TomlComments("要监听的发送者uid以及nga昵称")
  var watch: MutableMap<String, String> = mutableMapOf(
    "42382305" to "xiwang399",
    "40785736" to "安kuzuha",
    "64124793" to "星泠鑫"
  ),
  @TomlComments("已发送的缓存")
  var cache: MutableList<Pair<Int, String>> = mutableListOf()
)