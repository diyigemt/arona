package net.diyigemt.arona.config

import net.diyigemt.arona.entity.NudgeMessage
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.event.EventPriority

@ValueDescription("注意总出率加起来要等于100, 2,3星限定出率要低于其总出率")
object AronaGachaConfig: AutoSavePluginConfig("arona-gacha") {

  @ValueDescription("1星总出率百分比")
  var star1Rate: Float by value(79F)

  @ValueDescription("2星总出率百分比")
  var star2Rate: Float by value(18.5F)

  @ValueDescription("3星总出率百分比")
  var star3Rate: Float by value(2.5F)

  @ValueDescription("2星限定出率百分比")
  var star2PickupRate: Float by value(3F)

  @ValueDescription("3星限定出率百分比")
  var star3PickupRate: Float by value(0.7F)

  var defaultActivePool: Int by value(1)

  var maxDot: Int = 10

  fun init() {
    maxDot = listOf<Int>(
      getDotPosition(star1Rate.toString()),
      getDotPosition(star2Rate.toString()),
      getDotPosition(star3Rate.toString()),
      getDotPosition(star2PickupRate.toString()),
      getDotPosition(star3PickupRate.toString()),
    ).maxOf { it }
  }

  private fun getDotPosition(s: String): Int = if (s.indexOf(".") == -1) 1 else s.length - s.indexOf(".") - 1


}