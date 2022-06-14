package net.diyigemt.arona.config

import net.diyigemt.arona.entity.NudgeMessage
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.event.EventPriority
import java.util.*

object AronaGachaLimitConfig: AutoSavePluginConfig("arona-gacha-limit") {

  @ValueDescription("次数记录")
  var record: MutableList<Pair<Long, Int>> by value()

  @ValueDescription("上次更新记录")
  var lastUpdate: Int by value()

  @ValueDescription("每日限制次数")
  var limit: Int by value()

  fun update() {
    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    if (today == lastUpdate) return
    lastUpdate = today
    record = record.map {
      (it.first) to 0
    }.toMutableList()
  }

}