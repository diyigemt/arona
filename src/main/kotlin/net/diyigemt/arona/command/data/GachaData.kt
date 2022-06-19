package net.diyigemt.arona.command.data

import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.gacha.GachaHistory
import net.diyigemt.arona.db.gacha.GachaHistoryTable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import org.jetbrains.exposed.sql.and

object GachaData: AutoSavePluginConfig("arona-gacha") {
  @ValueDescription("用于保存抽卡的历史数据:次数,3星个数")
  val history: MutableList<Triple<Long, Int, Int>> by value()
  @ValueDescription("用于保存抽到pick次数")
  val dog: MutableList<Pair<Long, Int>> by value()

  fun getHistory(userId: Long, targetPool: Int = AronaGachaConfig.defaultActivePool): GachaHistory = query {
    val findList = GachaHistory.find { (GachaHistoryTable.id eq userId) and (GachaHistoryTable.pool eq targetPool) }.toList()
    if (findList.isEmpty()) return@query GachaHistory.new(userId) {
      points = 0
      count3 = 0
      dog = 0
      pool = 1
    }
    findList[0]
  }!!

  fun updateHistory(userId: Long, pool: Int = AronaGachaConfig.defaultActivePool, addPoints: Int = 10, addCount3: Int = 0, dog: Boolean = false) {
    query {
      val target = GachaHistory.findById(userId) ?: return@query
      target.points += addPoints
      target.count3 += addCount3
      if (dog && target.dog != 0) {
        target.dog = target.points
      }
    }
  }

  fun getDogCall(pool: Int = AronaGachaConfig.defaultActivePool): List<GachaHistory> = query {
    GachaHistory.find { GachaHistoryTable.pool eq pool }.toList().sortedBy { it.dog }
  }!!


//  fun getHistory(userId: Long): Triple<Long, Int, Int>? {
//    val filter = history.filter { it.first == userId }
//    return if (filter.isEmpty()) null else filter[0]
//  }

  fun putHistory(data: Triple<Long, Int, Int>): Unit {
    val filter = history.filter { it.first == data.first }
    if (filter.isEmpty()) {
      history.add(data)
    } else {
      history.remove(filter[0])
      history.add(data)
    }
  }

  fun getHistoryAll(pool: Int = AronaGachaConfig.defaultActivePool) = query {
    GachaHistory.find { GachaHistoryTable.pool eq pool }.toList().filter { it.count3 != 0 } .sortedBy {
      it.points / it.count3
    }
  }!!

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