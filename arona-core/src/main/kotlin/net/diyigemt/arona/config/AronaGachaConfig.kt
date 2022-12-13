package net.diyigemt.arona.config

import net.diyigemt.arona.Arona.reload
import net.diyigemt.arona.interfaces.Initialize
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

@ValueDescription("注意总出率加起来要等于100, 2,3星限定出率要低于其总出率")
object AronaGachaConfig: AutoSavePluginConfig("arona-gacha"), Initialize {

  @ValueDescription("1星总出率百分比")
  var star1Rate: Float by value(78.5F)

  @ValueDescription("2星总出率百分比")
  var star2Rate: Float by value(18.5F)

  @ValueDescription("3星总出率百分比")
  var star3Rate: Float by value(3.0F)

  @ValueDescription("2星限定出率百分比")
  var star2PickupRate: Float by value(3F)

  @ValueDescription("3星限定出率百分比")
  var star3PickupRate: Float by value(0.7F)

  @ValueDescription("当前激活的池子")
  var activePool: Int by value(1)

  @ValueDescription("撤回结果信息防止刷屏 撤回时间间隔(单位为秒) 为0表示不撤回")
  var revokeTime: Int by value(10)

  @ValueDescription("每日限制次数, 0表示不限制")
  var limit: Int by value(0)

  @ValueDescription("上次限制更新时间,自动维护")
  var day: Int by value(0)


  var maxDot: Int = 10

  override val priority: Int = 99
  override fun init() {
    reload()
    maxDot = listOf(
      getDotPosition(star1Rate.toString()),
      getDotPosition(star2Rate.toString()),
      getDotPosition(star3Rate.toString()),
      getDotPosition(star2PickupRate.toString()),
      getDotPosition(star3PickupRate.toString()),
    ).maxOf { it }
  }

  private fun getDotPosition(s: String): Int = if (s.indexOf(".") == -1) 1 else s.length - s.indexOf(".") - 1


}