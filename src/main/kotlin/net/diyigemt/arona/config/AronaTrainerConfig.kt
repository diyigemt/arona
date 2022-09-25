package net.diyigemt.arona.config

import net.diyigemt.arona.command.TrainerCommand
import net.diyigemt.arona.entity.TrainerOverride
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaTrainerConfig: AutoSavePluginConfig("arona-trainer") {

  @ValueDescription("当对应图片不存在时是否提示\"请联系管理员添加\"")
  val tipWhenNull: Boolean by value(true)

  @ValueDescription("模糊查询的数据来源 \"ALL\":作者设置的与自行添加的 \"LOCAL_CONFIG\":仅使用自行添加的 \"REMOTE\":仅使用作者添加的")
  val fuzzySearchSource: TrainerCommand.FuzzySearchSource by value(TrainerCommand.FuzzySearchSource.ALL)

  @ValueDescription("覆盖/攻略指令的功能来自己设置方便的名字或者用来整蛊")
  val override: List<TrainerOverride> by value()

}