package net.diyigemt.arona.config

import net.diyigemt.arona.entity.ServerLocale
import net.diyigemt.arona.quartz.NotifyType
import net.diyigemt.arona.util.ActivityUtil
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaNotifyConfig: AutoSavePluginConfig("arona-notify") {

  @ValueDescription("是否启用每日防侠提醒")
  val enableEveryDay: Boolean by value(true)

  @ValueDescription("每日提醒类型,可选ALL(提醒所有时段),ONLY_24H(仅提醒24小时内),ONLY_48H(仅提醒48小时内)")
  val notifyType: NotifyType by value(NotifyType.ONLY_48H)

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

  @ValueDescription("双倍掉落防侠提醒的时间(因为一般是晚上3点结束)")
  val dropNotify: Int by value(22)

  @ValueDescription("\"/活动\"指令的默认目标服务器,可选值为 \"JP\"和\"GLOBAL\"")
  val defaultActivityCommandServer: ServerLocale by value(ServerLocale.JP)

  @ValueDescription("\"/活动 jp\"指令的默认数据源,可选值为 \"B_WIKI\", \"WIKI_RU\", 和\"GAME_KEE\"")
  val defaultJPActivitySource: ActivityUtil.ActivityJPSource by value(ActivityUtil.ActivityJPSource.GAME_KEE)

}