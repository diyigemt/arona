package net.diyigemt.arona.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import net.diyigemt.arona.command.cache.GachaCache
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.config.AronaGachaLimitConfig
import net.diyigemt.arona.constant.GachaConstant
import net.diyigemt.arona.db.model.gacha.GachaCharacter

object GachaUtil {

  fun pikerUp(): JsonElement {
    val random = (0..1000).random()
    var target: JsonElement = GachaConstant.REWARD_LIST_THREE_STAR;
    if (random in (0 .. GachaConstant.PICK_UP_ONE_STAR)) {
      target = GachaConstant.REWARD_LIST_ONE_STAR
    } else if (random in (GachaConstant.PICK_UP_ONE_STAR .. GachaConstant.PICK_UP_TOW_STAR)) {
      target = GachaConstant.REWARD_LIST_TWO_STAR
    } else if (random in (GachaConstant.PICK_UP_TOW_STAR .. GachaConstant.PICK_UP_THREE_STAR)) {
      target = GachaConstant.REWARD_LIST_THREE_STAR
    } else {
      return Json.parseToJsonElement("{\"star\":3,\"name\":\"初音(Pick Up)\"}")
    }
    val index = (0 until target.jsonArray.size).random()
    return target.jsonArray[index]
  }

  fun pickUpTwoStar(): JsonElement {
    val index = (0 until GachaConstant.REWARD_LIST_TWO_STAR.jsonArray.size).random()
    return GachaConstant.REWARD_LIST_TWO_STAR.jsonArray[index]
  }

  fun resultData2String(result: JsonElement): String {
    val name = result.jsonObject["name"].toString().replace("\"", "")
    val stars = result.jsonObject["star"]
    return "$name($stars${GachaConstant.star})"
  }

  fun pickup(): GachaCharacter {
    val star1List = GachaCache.star1List
    val star2List = GachaCache.star2List
    val star3List = GachaCache.star3List
    val star2PickupList = GachaCache.star2PickupList
    val star3PickupList = GachaCache.star3PickupList
    val maxDot = AronaGachaConfig.maxDot
    val star1Rate = (AronaGachaConfig.star1Rate * maxDot).toInt()
    val star2Rate = (AronaGachaConfig.star2Rate * maxDot).toInt()
    val star3Rate = (AronaGachaConfig.star3Rate * maxDot).toInt()
    val star2PickupRate = (AronaGachaConfig.star2PickupRate * maxDot).toInt()
    val star3PickupRate = (AronaGachaConfig.star3PickupRate * maxDot).toInt()
    return when ((0 until pow10(maxDot)).random()) {
      in (0 until star1Rate) -> rollList(star1List)
      in (star1Rate until (star1Rate + star2Rate)) -> {
        if (star2PickupList.size != 0) {
          return if ((0 until (star2Rate)).random() < star2PickupRate) {
            // 抽到pickup
            rollList(star2PickupList)
          } else {
            rollList(star2List)
          }
        }
        rollList(star2List)
      }
      else -> {
        if (star3PickupList.size != 0) {
          return if ((0 until (star3Rate)).random() < star3PickupRate) {
            // 抽到pickup
            rollList(star3PickupList)
          } else {
            rollList(star3List)
          }
        }
        rollList(star3List)
      }
    }
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

  private fun rollList(list: List<GachaCharacter>) = list[(list.indices).random()]

  private fun pow10(pow: Int) = Array<Int>(pow) { 10 }.sum()

  fun result2String(character: GachaCharacter) = "${character.name}(${character.star}${GachaConstant.star})"

}