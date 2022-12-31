package net.diyigemt.arona.entity

/**
 * 存储Bot与Group的对应关系
 *
 * 一个Bot可以对应多个群, 单个群只能关联一个Bot
 *
 * 可能以后会支持细化到bot和群的配置文件吧, 现在只细化到群
 */
data class BotGroupConfig (
  val bot: Long,
  val groups: List<Long>
)