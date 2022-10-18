package net.diyigemt.arona.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaTarotConfig: AutoSavePluginConfig("arona-tarot") {

  @ValueDescription("塔罗牌每天是否只能抽一张(多次抽取结果相同)")
  val dayOne: Boolean by value(false)

  @ValueDescription("顺带发送P站Shi0n老师画的塔罗牌图片")
  val image: Boolean by value(true)

}