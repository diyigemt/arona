@file:Suppress("DuplicatedCode")

package net.diyigemt.arona.util

import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.gacha.*
import net.diyigemt.arona.interfaces.ConfigReader
import net.diyigemt.arona.interfaces.Initialize
import net.diyigemt.arona.interfaces.getGroupConfig
import net.diyigemt.arona.interfaces.setGroupConfig
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

object GachaUtil: Initialize, ConfigReader {
  private const val star = "★"
  private val star1List: MutableList<GachaCharacters> = mutableListOf()
  private val star2List: MutableList<GachaCharacters> = mutableListOf()
  private val star3List: MutableList<GachaCharacters> = mutableListOf()

  fun pickup(group: Long, time: Int = 1): List<GachaCharacters> {
    val star1Rate = getGroupConfig<Float>("start1Rate", group)
    val star2Rate = getGroupConfig<Float>("star2Rate", group)
    val star3Rate = getGroupConfig<Float>("star3Rate", group)
    val star2PickupRate = getGroupConfig<Float>("star2PickupRate", group)
    val star3PickupRate = getGroupConfig<Float>("star3PickupRate", group)

    val maxDot = listOf(
      getDotPosition(star1Rate),
      getDotPosition(star2Rate),
      getDotPosition(star3Rate),
      getDotPosition(star2PickupRate),
      getDotPosition(star3PickupRate),
    ).maxOf { it }.let { pow10(it) }

    val groupPool = getGroupConfig<Int>("pool", group)
    val limit = DataBaseProvider.query {
      GachaCharactersTable
        .innerJoin(GachaPoolCharactersTable)
        .innerJoin(GachaPoolsTable)
        .select { GachaPoolsTable.id eq groupPool }
        .map {
          GachaCharacters.wrapRow(it)
        }
    }!!
    val star2PickupList = limit.filter { it.star == 2 }.toMutableList()
    val star3PickupList = limit.filter { it.star == 3 }.toMutableList()

    val star1RateInt = (star1Rate * maxDot).toInt()
    val star2RateInt = (star2Rate * maxDot).toInt()
    val star2PickupRateInt = (star2PickupRate * maxDot).toInt()
    val star3RateInt = (star3Rate * maxDot).toInt()
    val star3PickupRateInt = (star3PickupRate * maxDot).toInt()
    val result = (0 until time).map {
      when ((0 until 100 * maxDot).random()) {
        in (0 until star1RateInt) -> pickup1()
        in (star1RateInt until (star1RateInt + star2RateInt)) -> pickup2(star2RateInt, star2PickupRateInt, star2PickupList)
        else -> pickup3(star3RateInt, star3PickupRateInt, star3PickupList)
      }
    }.toMutableList()
    // 保底
    for (i in 0 until time step 10) {
      val end = if (i + 10 > time) time else i + 10
      val sub = result.subList(i, end)
      if (sub.sumOf { it.star } == 10 && end == 10) {
        sub[9] = pickup2(star2RateInt, star2PickupRateInt, star2PickupList)
      }
    }
    return result
  }

  private fun pickup3(rate: Int, pickupRate: Int ,pickupList: List<GachaCharacters>): GachaCharacters {
    return pickup(star3List, pickupList, rate, pickupRate)
  }

  fun pickup2(rate: Int, pickupRate: Int ,pickupList: List<GachaCharacters>): GachaCharacters {
    return pickup(star2List, pickupList, rate, pickupRate)
  }

  private fun pickup1(): GachaCharacters {
    return pickup(star1List, null, 0)
  }

  fun pickup(list: List<GachaCharacters>, pickupList: List<GachaCharacters>?, rate: Int, pickupRate: Int = 0) =
    if (!pickupList.isNullOrEmpty()) {
      if ((0 until rate + pickupRate).random() < pickupRate) {
        rollList(pickupList)
      } else {
        rollList(list)
      }
    } else {
      rollList(list)
    }

  fun checkTime(userId: Long, group: Long, time: Int = 10): Int {
    updateGachaLimit(group)
    val limit = getGroupConfig<Int>("limit", group)
    if (limit == 0) return time
    val record = getLimit(userId, group)
    val history = record.count
    val time2 = history + time
    val add = if ((limit - time2) >= 0) {
      time
    } else {
      limit - history
    }
    updateLimit(userId, group, add)
    return add
  }

  private fun getLimit(userId: Long, group0: Long): GachaLimit = DataBaseProvider.query {
    val findList =
      GachaLimit.find { (GachaLimitTable.id eq userId) and (GachaLimitTable.group eq group0) }
        .toList()
    if (findList.isEmpty()) return@query GachaLimit.new(userId) {
      group = group0
      count = 0
    }
    findList[0]
  }!!

  private fun updateLimit(userId: Long, group0: Long, add: Int) {
    DataBaseProvider.query {
      val target =
        GachaLimit.find { (GachaLimitTable.id eq userId) and (GachaLimitTable.group eq group0) }
          .toList()[0]
      target.count += add
    }
  }

  fun getHistory(userId: Long, group0: Long, targetPool: Int = getGroupConfig("pool", group0)): GachaHistory = DataBaseProvider.query {
    val findList =
      GachaHistory.find { (GachaHistoryTable.id eq userId) and (GachaHistoryTable.pool eq targetPool) and (GachaHistoryTable.group eq group0) }
        .toList()
    if (findList.isEmpty()) return@query GachaHistory.new(userId) {
      group = group0
      points = 0
      count3 = 0
      dog = 0
      pool = targetPool
    }
    findList[0]
  }!!

  fun updateHistory(userId: Long, group: Long, pool: Int = getGroupConfig("pool", group), addPoints: Int = 10, addCount3: Int = 0, dog: Boolean = false) {
    DataBaseProvider.query {
      val target =
        GachaHistory.find { (GachaHistoryTable.id eq userId) and (GachaHistoryTable.pool eq pool) and (GachaHistoryTable.group eq group) }
          .toList()[0]
      target.points += addPoints
      target.count3 += addCount3
      if (dog && target.dog == 0) {
        target.dog = target.points
      }
    }
  }

  fun getDogCall(group: Long, pool: Int = getGroupConfig("pool", group)): List<GachaHistory> = DataBaseProvider.query {
    GachaHistory.find { (GachaHistoryTable.pool eq pool) and (GachaHistoryTable.group eq group) }.toList()
      .sortedBy { it.dog }
  }!!


  fun getHistoryAll(group: Long, pool: Int = getGroupConfig("pool", group)) = DataBaseProvider.query {
    GachaHistory.find { (GachaHistoryTable.pool eq pool) and (GachaHistoryTable.group eq group) }.toList().sortedBy {
      if (it.count3 == 0) 999 else it.points / it.count3
    }
  }!!

  fun resultData2String(result: GachaCharacters) = "${result.name}(${result.star}${star})${if (hitPickup(result)) "(pick up)" else ""}"

  fun hitPickup(result: GachaCharacters) = DataBaseProvider.query {
    GachaCharacters.find {
      GachaCharactersTable.id eq result.id
    }.first().limit
  }!!

  fun mapStudentInfo(student: GachaCharacters) = mapStudentInfo(student.name, student.star)

  fun mapStudentInfo(name: String, star: Int) = "${name}(${star}${this.star})"

  private fun updateGachaList() {
    val all = DataBaseProvider.query {
      GachaCharacters.find { GachaCharactersTable.limit eq false }
    }!!
    star1List.clear()
    star1List.addAll(all.filter { it.star == 1 })
    star2List.clear()
    star2List.addAll(all.filter { it.star == 2 })
    star3List.clear()
    star3List.addAll(all.filter { it.star == 3 })
  }

  private fun rollList(list: List<GachaCharacters>) = list[(list.indices).random()]

  private fun pow10(pow: Int) = Array(pow) { 10 }.sum()

  private fun getDotPosition(num: Float): Int {
    val s = num.toString()
    return if (s.indexOf(".") == -1) 1 else s.length - s.indexOf(".") - 1
  }

  private fun updateGachaLimit(group: Long?) {
    if (group == null) {
      forceUpdateGachaLimit()
    } else {
      val today = TimeUtil.today()
      val config = getGroupConfig<Int>("today", group)
      if (config == today) return
      setGroupConfig("today", group, today)
      forceUpdateGachaLimit(group)
    }
  }

  fun forceUpdateGachaLimit(group0: Long? = null) {
    DataBaseProvider.query {
      if (group0 == null) {
        GachaLimitTable.update {
          it[count] = 0
        }
      } else {
        GachaLimitTable.update({ GachaLimitTable.group eq group0 }) {
          it[count] = 0
        }
      }
    }
  }

  override val configPrefix: String = "gacha"
  override val priority: Int = 11
  override fun init() {
    updateGachaList()
  }

}