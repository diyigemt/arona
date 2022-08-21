package net.diyigemt.arona.entity

data class Activity(
  var content: String,
  var time: String,
  var type: ActivityType = ActivityType.NULL,
  var serverLocale: ServerLocale = ServerLocale.JP,
  val katakana: String = "",
  val description : String = ""
)

enum class ServerLocale(val serverName: String) {
  GLOBAL("国际服"), JP("日服")
}

enum class ActivityType(val level: Int) {
  NULL(1),
  N2_3(1),
  H2_3(1),
  SPECIAL_DROP(1), // 特别作战(钱本经验本)
  WANTED_DROP(1), // 悬赏通缉
  COLLEGE_EXCHANGE_DROP(1), // 学院交流
  SCHEDULE(1), // 课程表
  DECISIVE_BATTLE(3), // 总力战
  JOINT_EXERCISES(1), // 合同火力演习
  PICK_UP(3), // 卡池
  KABALA(2), // 十字神明特殊活动
  MAINTENANCE(4), // 维护
  ACTIVITY(2) // 普通活动
}