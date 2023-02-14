package net.diyigemt.arona.config

import net.diyigemt.arona.entity.TrainerOverride
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AronaTrainerConfig: AutoSavePluginConfig("arona-trainer") {

  @ValueDescription("当对应图片不存在时是否提示模糊搜索结果, 若连模糊搜索结果都没有显示\"请联系管理员添加\"")
  val tipWhenNull: Boolean by value(true)

  @ValueDescription("模糊搜索结果撤回时间, 设置为0代表不撤回. 单位是秒")
  val tipRevokeTime: Int by value(10)

  @ValueDescription("等待用户对模糊搜索结果的响应时间, 设置为0代表关闭响应. 单位是秒")
  val tipResponseWaitTime: Int by value(10)

  @ValueDescription("覆盖/攻略指令的功能来自己设置方便的名字或者用来整蛊")
  val override: List<TrainerOverride> by value()

}