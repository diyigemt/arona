package net.diyigemt.arona.config

import net.diyigemt.arona.Arona.reload
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

@ValueDescription("注意总出率加起来要等于100, 2,3星限定出率要低于其总出率")
object AronaGachaConfig: AutoSavePluginConfig("arona-gacha") {

  @ValueDescription("1星总出率百分比")
  val star1Rate: Float by value(79F)

  @ValueDescription("2星总出率百分比")
  val star2Rate: Float by value(18.5F)

  @ValueDescription("3星总出率百分比")
  val star3Rate: Float by value(2.5F)

  @ValueDescription("2星限定出率百分比")
  val star2PickupRate: Float by value(3F)

  @ValueDescription("3星限定出率百分比")
  val star3PickupRate: Float by value(0.7F)

  @ValueDescription("当前激活的池子")
  var activePool: Int by value(1)

  @ValueDescription("是否撤回结果信息防止刷屏")
  val revoke: Boolean by value(true)

  @ValueDescription("撤回时间间隔(单位为秒)")
  val revokeTime: Int by value(10)

  var maxDot: Int = 10

  fun init() {
    reload()
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