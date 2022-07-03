package net.diyigemt.arona.command.data

import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.gacha.GachaHistory
import net.diyigemt.arona.db.gacha.GachaHistoryTable
import org.jetbrains.exposed.sql.and

object GachaData {

  fun getHistory(group0: Long, userId: Long, targetPool: Int = AronaGachaConfig.activePool): GachaHistory = query {
    val findList = GachaHistory.find { (GachaHistoryTable.id eq userId) and (GachaHistoryTable.pool eq targetPool) and (GachaHistoryTable.group eq group0) }.toList()
    if (findList.isEmpty()) return@query GachaHistory.new(userId) {
      group = group0
      points = 0
      count3 = 0
      dog = 0
      pool = targetPool
    }
    findList[0]
  }!!

  fun updateHistory(group: Long, userId: Long, pool: Int = AronaGachaConfig.activePool, addPoints: Int = 10, addCount3: Int = 0, dog: Boolean = false) {
    query {
      val target = GachaHistory.find { (GachaHistoryTable.id eq userId) and (GachaHistoryTable.pool eq pool) and (GachaHistoryTable.group eq group) }.toList()[0]
      target.points += addPoints
      target.count3 += addCount3
      if (dog && target.dog == 0) {
        target.dog = target.points
      }
    }
  }

  fun getDogCall(group: Long, pool: Int = AronaGachaConfig.activePool): List<GachaHistory> = query {
    GachaHistory.find { (GachaHistoryTable.pool eq pool) and (GachaHistoryTable.group eq group) }.toList().sortedBy { it.dog }
  }!!


  fun getHistoryAll(group: Long, pool: Int = AronaGachaConfig.activePool) = query {
    GachaHistory.find { (GachaHistoryTable.pool eq pool) and (GachaHistoryTable.group eq group) }.toList().sortedBy {
      if (it.count3 == 0) 999 else it.points / it.count3
    }
  }!!
}