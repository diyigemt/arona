package net.diyigemt.arona.entity

data class Activity(
  val content: String,
  val time: String,
  val type: ActivityType = ActivityType.NULL,
  val serverLocale: ServerLocale = ServerLocale.JP
)

enum class ServerLocale {
  GLOBAL, JP
}

enum class ActivityType(var level: Int) {
  NULL(0),
  N2_3(2),
  H2_3(2),
  SPECIAL_DROP(2), // 特别作战(钱本经验本)
  WANTED_DROP(2), //悬赏通缉
  COLLEGE_EXCHANGE_DROP(2), //学院交流
  DECISIVE_BATTLE(3), // 总力战
  JOINT_EXERCISES(2), // 合同火力演习
  PICK_UP(2), // 卡池
  KABALA(2), // 十字神明特殊活动
  MAINTENANCE(4), // 维护
}