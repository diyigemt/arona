package net.diyigemt.arona.command.data

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object GachaData: AutoSavePluginConfig("arona-gacha") {
  @ValueDescription("用于保存抽卡的历史数据:次数,123星个数")
  val history: MutableMap<Long, Int> by value()
  @ValueDescription("用于保存抽到pick次数")
  val dog: MutableList<Pair<Long, Int>> by value()

  fun getHistory(userId: Long): Int? {
    return history[userId]
  }

  fun putHistory(kay: Long, data: Int): Unit {
    history[kay] = data
  }

  // 获取抽到pickup最少的前5名
  fun saveDog(userId: Long, times: Int): Unit {
    val filter = dog.filter { it.first == userId }
    if (filter.isEmpty()) {
      dog.add(Pair(userId, times))
    }
  }

  fun getDogCall(): List<Pair<Long, Int>> {
    dog.sortBy { it.second }
    return dog
  }
}