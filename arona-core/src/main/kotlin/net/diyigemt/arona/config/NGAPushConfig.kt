package net.diyigemt.arona.config

import net.diyigemt.arona.advance.NGAImageTranslatePusher
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object NGAPushConfig: AutoSavePluginConfig("nga") {

  @ValueDescription("你自己的nga uid")
  val uid: String by value("")

  @ValueDescription("登录后产生的cookie, 可以从名叫\"ngaPassportCid\"的cookie中获取")
  val cid: String by value("")

  @ValueDescription("扫描周期(单位min)")
  val checkInterval: Int by value(30)

  @ValueDescription("数据源(主站寄了的时候可以换一下),可选\"MAIN\"(主站)和\"SUB\"(备用站)")
  val source: NGAImageTranslatePusher.NGASource by value(NGAImageTranslatePusher.NGASource.SUB)

  @ValueDescription("要监听的发送者uid以及nga昵称")
  val watch: MutableMap<String, String> by value(
    mutableMapOf(
      "42382305" to "xiwang399",
      "40785736" to "安kuzuha",
      "64124793" to "星泠鑫"
    )
  )

  @ValueDescription("已发送的缓存")
  val cache: MutableList<Pair<Int, String>> by value()

}