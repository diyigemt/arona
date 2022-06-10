package net.diyigemt.arona.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import net.diyigemt.arona.command.cache.GachaCache
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.constant.GachaConstant
import net.diyigemt.arona.db.model.GachaCharacter
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random

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
      return Json.parseToJsonElement("{\"star\":3,\"name\":\"亚津子(Pick Up)\"}")
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
    val random = (0 until pow10(maxDot)).random()
    val targetList = star1List
    if (random in (star1Rate until star1Rate + star2Rate)) {
      // 有pickup
      if (star2PickupList.size != 0) {
        val random = (0 until (star2Rate)).random()
        // 抽到pickup
        if (random < star2PickupRate) {
          return rollList(star2PickupList)
        }
        return rollList(star2List)
      }
      val nextFloat = Random(LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"))).nextFloat()
    }
    return star2PickupList[0]
  }

  private fun rollList(list: List<GachaCharacter>) = list[(list.indices).random()]

  private fun pow10(pow: Int) = Array<Int>(pow) { 10 }.sum()

  fun result2String(character: GachaCharacter) = "${character.name}(${character.star}${GachaConstant.star})"

}