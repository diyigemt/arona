package net.diyigemt.arona.config

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

}