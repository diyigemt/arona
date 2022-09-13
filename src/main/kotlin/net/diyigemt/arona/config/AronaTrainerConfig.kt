package net.diyigemt.arona.config

import net.diyigemt.arona.entity.TrainerOverride
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaTrainerConfig: AutoSavePluginConfig("arona-trainer") {

  @ValueDescription("覆盖/攻略指令的功能来自己设置方便的名字或者用来整蛊")
  val override: List<TrainerOverride> by value()

}