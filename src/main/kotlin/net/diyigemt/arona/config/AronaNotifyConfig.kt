package net.diyigemt.arona.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaNotifyConfig: AutoSavePluginConfig("arona-notify") {

  @ValueDescription("是否启用防侠提醒")
  val enable: Boolean by value(true)

  @ValueDescription("是否启用每日防侠提醒")
  val enableEveryDay: Boolean by value(true)

  @ValueDescription("每日防侠提醒的时间(同时也是每日数据更新时间)")
  val everyDayHour: Int by value(8)

  @ValueDescription("启用日服防侠提醒")
  val enableJP: Boolean by value(true)

  @ValueDescription("日服防侠提醒开头文字")
  val notifyStringJP: String by value("arona的防侠预警(日服)")

  @ValueDescription("启用国际服防侠提醒")
  val enableEN: Boolean by value(true)

  @ValueDescription("国际服防侠提醒开头文字")
  val notifyStringEN: String by value("arona的防侠预警(国际服)")

  @ValueDescription("日服防侠提醒的时间")
  val jpHour: Int by value(22)

}