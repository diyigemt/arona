package net.diyigemt.arona.command

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.constant.GachaConstant
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.At
@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
object GachaCommand: CompositeCommand(
  Arona, "gacha", "抽卡",
  description = "模拟抽卡"
) {
  private const val star = "★"
  @SubCommand("单抽")
  suspend fun UserCommandSender.test() {
    subject.sendMessage(At(user).plus(pikerUp()))
  }

  @SubCommand("十连")
  suspend fun UserCommandSender.spins() {
    subject.sendMessage(At(user).plus("没写好"))
  }

  private fun pikerUp(): String {
    val random = (0..100).random()
    var target: JsonElement = GachaConstant.REWARD_LIST_THREE_STAR;
    if (random.toFloat() in ((0).toFloat() .. GachaConstant.PICK_UP_ONE_STAR)) {
      target = GachaConstant.REWARD_LIST_ONE_STAR
    } else if (random.toFloat() in (GachaConstant.PICK_UP_ONE_STAR .. GachaConstant.PICK_UP_TOW_STAR)) {
      target = GachaConstant.REWARD_LIST_TWO_STAR
    }
    val index = (0 until target.jsonArray.size).random()
    val name = target.jsonArray[index].jsonObject["name"].toString().replace("\"", "")
    val stars = target.jsonArray[index].jsonObject["star"]
    return "$name($stars$star)"
  }
}