package net.diyigemt.arona.command.data

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object GachaData: AutoSavePluginConfig("arona-gacha") {
  @ValueDescription("用于保存抽卡的历史数据:次数,3星个数")
  val history: MutableList<Triple<Long, Int, Int>> by value()
  @ValueDescription("用于保存抽到pick次数")
  val dog: MutableList<Pair<Long, Int>> by value()

  fun getHistory(userId: Long): Triple<Long, Int, Int>? {
    val filter = history.filter { it.first == userId }
    return if (filter.isEmpty()) null else filter[0]
  }

  fun putHistory(data: Triple<Long, Int, Int>): Unit {
    val filter = history.filter { it.first == data.first }
    if (filter.isEmpty()) {
      history.add(data)
    } else {
      history.remove(filter[0])
      history.add(data)
    }
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

  fun getHistoryAll(): List<Triple<Long, Int, Int>> {
    return history.sortedBy { if (it.third == 0) 0 else (it.second / it.third) }
  }
}