package net.diyigemt.arona.config

import net.diyigemt.arona.entity.NudgeMessage
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.event.EventPriority

object AronaHentaiConfig: AutoSavePluginConfig("arona-hentai") {

  @ValueDescription("要被骂的id")
  var listen: MutableList<Long> by value()
  @ValueDescription("返回被骂的信息")
  var messageList: MutableList<NudgeMessage> by value()
  @ValueDescription("是否启用")
  var enable: Boolean by value(false)

  /**
   * 优先级 默认为高
   * @see EventPriority
   */
  @ValueDescription("事件优先级 从高到低可选 HIGHEST, HIGH, NORMAL, LOW, LOWEST, MONITOR\n" +
    "设置后需要重启插件生效")
  var priority: EventPriority by value(EventPriority.HIGH)

}