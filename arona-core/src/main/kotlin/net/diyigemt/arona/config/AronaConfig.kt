package net.diyigemt.arona.config

import net.diyigemt.arona.advance.NGAImageTranslatePusher
import net.diyigemt.arona.annotations.ConfigKey
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaConfig : AutoSavePluginConfig("arona") {

  @ValueDescription("运行arona的qq")
  var qq: Long by value()

  @ValueDescription("具有管理员权限的qq号")
  var managerGroup: MutableList<Long> by value()

  @ValueDescription("自动检查更新的时间")
  var updateCheckTime: Int by value(8)

  @ValueDescription("是否允许arona收集匿名统计信息")
  var sendStatus: Boolean by value(false)

  @ValueDescription("远端操作查询间隔 设置为0表示不开启, 单位是小时")
  var remoteCheckInterval: Int by value(1)

  @ValueDescription("webui监听端口")
  @ConfigKey("webui.port")
  val webuiPort: Int by value(8080)

  @ValueDescription("你自己的nga uid")
  @ConfigKey("nga.uid")
  var ngaUid: String by value("")

  @ValueDescription(
    """
      登录后产生的cookie, 可以从名叫\"ngaPassportCid\"的cookie中获取,
      具体配置方法看这里 https://github.com/diyigemt/arona/blob/master/doc/using.md#nga-config
    """
  )
  @ConfigKey("nga.cid")
  var ngaCid: String by value("")

  @ValueDescription("扫描周期(单位min)")
  @ConfigKey("nga.checkInterval")
  var ngaCheckInterval: Int by value(30)

  @ValueDescription("nga数据源(主站寄了的时候可以换一下),可选\"MAIN\"(主站)和\"SUB\"(备用站)")
  @ConfigKey("nga.source")
  var ngaSource: String by value(NGAImageTranslatePusher.NGASource.SUB.name)

  @ValueDescription("要监听的发送者uid以及nga昵称")
  var ngaWatch: MutableMap<String, String> by value(
    mutableMapOf(
      "42382305" to "xiwang399",
      "40785736" to "安kuzuha",
      "64124793" to "星泠鑫"
    )
  )
  @ValueDescription("已发送的缓存")
  var ngaCache: MutableList<Pair<Int, String>> by value(mutableListOf())

}