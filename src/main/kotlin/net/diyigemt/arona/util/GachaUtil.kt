@file:Suppress("DuplicatedCode")

package net.diyigemt.arona.util

import net.diyigemt.arona.command.cache.GachaCache
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.config.AronaGachaLimitConfig
import net.diyigemt.arona.constant.GachaConstant
import net.diyigemt.arona.db.gacha.GachaCharacter

object GachaUtil {

  fun pickup(): GachaCharacter {
    val maxDot = pow10(AronaGachaConfig.maxDot)
    val star1Rate = (AronaGachaConfig.star1Rate * maxDot).toInt()
    val star2Rate = (AronaGachaConfig.star2Rate * maxDot).toInt()
    return when ((0 until 100 * maxDot).random()) {
      in (0 until star1Rate) -> pickup1()
      in (star1Rate until (star1Rate + star2Rate)) -> pickup2()
      else -> pickup3()
    }
  }

  fun pickup3(): GachaCharacter {
    val maxDot = pow10(AronaGachaConfig.maxDot)
    val star3List = GachaCache.star3List
    val star3PickupList = GachaCache.star3PickupList
    val star3Rate = (AronaGachaConfig.star3Rate * maxDot).toInt()
    val star3PickupRate = (AronaGachaConfig.star3PickupRate * maxDot).toInt()
    return pickup(star3List, star3PickupList, star3Rate, star3PickupRate)
  }

  fun pickup2(): GachaCharacter {
    val maxDot = pow10(AronaGachaConfig.maxDot)
    val star2List = GachaCache.star2List
    val star2PickupList = GachaCache.star2PickupList
    val star2Rate = (AronaGachaConfig.star2Rate * maxDot).toInt()
    val star2PickupRate = (AronaGachaConfig.star2PickupRate * maxDot).toInt()
    return pickup(star2List, star2PickupList, star2Rate, star2PickupRate)
  }

  fun pickup1(): GachaCharacter {
    val maxDot = pow10(AronaGachaConfig.maxDot)
    val star1List = GachaCache.star1List
    val star1Rate = (AronaGachaConfig.star1Rate * maxDot).toInt()
    return pickup(star1List, null, star1Rate)
  }

  fun pickup(list: List<GachaCharacter>, pickupList: List<GachaCharacter>?, rate: Int, pickupRate: Int = 0) =
    if (pickupList != null && pickupList.isNotEmpty()) {
      if ((0 until rate).random() < pickupRate) {
        rollList(pickupList)
      } else {
        rollList(list)
      }
    } else {
      rollList(list)
    }

  fun checkTime(userId: Long, time: Int = 10): Int {
    AronaGachaLimitConfig.update()
    val limit = AronaGachaLimitConfig.limit
    val record = AronaGachaLimitConfig.record
    val filter = record.filter { it.first == userId }
    if (filter.isEmpty()) {
      return if ((limit - time) > 0) {
        record.add(Pair(userId, time))
        time
      } else {
        record.add(Pair(userId, limit))
        limit
      }
    }
    val target = filter[0]
    record.remove(target)
    val history = target.second
    val time2 = history + time
    return if ((limit - time2) >= 0) {
      record.add(Pair(userId, time2))
      time
    } else {
      record.add(Pair(userId, limit))
      limit - history
    }
  }

  fun resultData2String(result: GachaCharacter) = "${result.name}(${result.star}${GachaConstant.star})${if (hitPickup(result)) "(pick up)" else ""}"

  fun hitPickup(result: GachaCharacter) = GachaCache.star2PickupList.contains(result) || GachaCache.star3PickupList.contains(result)

  private fun rollList(list: List<GachaCharacter>) = list[(list.indices).random()]

  private fun pow10(pow: Int) = Array<Int>(pow) { 10 }.sum()

}