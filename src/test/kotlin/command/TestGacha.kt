package org.example.mirai.plugin.command

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import net.diyigemt.arona.command.constant.GachaConstant
import net.diyigemt.arona.command.constant.GachaConstant.REWARD_LIST_ONE_STAR
import net.diyigemt.arona.command.constant.GachaConstant.REWARD_LIST_THREE_STAR
import net.diyigemt.arona.command.constant.GachaConstant.REWARD_LIST_TWO_STAR
import org.junit.jupiter.api.Test

class TestGacha {
  @Test
  fun testGetElement() {
    val star = "★"
    val random = (0..100).random()
    var target: JsonElement = REWARD_LIST_THREE_STAR;
    if (random.toFloat() in ((0).toFloat() .. GachaConstant.PICK_UP_ONE_STAR)) {
      target = REWARD_LIST_ONE_STAR
    } else if (random.toFloat() in (GachaConstant.PICK_UP_ONE_STAR .. GachaConstant.PICK_UP_TOW_STAR)) {
      target = REWARD_LIST_TWO_STAR
    }
    val index = (0 until target.jsonArray.size).random()
    val name = target.jsonArray[index].jsonObject["name"].toString().replace("\"", "")
    val stars = target.jsonArray[index].jsonObject["star"]
    println("获得老婆: $name($stars$star)")
  }
}