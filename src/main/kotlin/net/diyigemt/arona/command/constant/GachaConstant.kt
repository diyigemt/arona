package net.diyigemt.arona.command.constant

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

object GachaConstant {
  const val PICK_UP_ONE_STAR = 790
  const val PICK_UP_TOW_STAR = PICK_UP_ONE_STAR + 185
  const val PICK_UP_THREE_STAR = PICK_UP_TOW_STAR + 25 - 7
  const val PICK_UP_PICK_UP = 1000
  const val star = "★"
  val REWARD_LIST_ONE_STAR: JsonElement = Json.parseToJsonElement("[{\"star\":1,\"name\":\"好美\"}," +
    "{\"star\":1,\"name\":\"春香\"}," +
    "{\"star\":1,\"name\":\"泉(泳装)\"}," +
    "{\"star\":1,\"name\":\"铃美\"}," +
    "{\"star\":1,\"name\":\"满\"}," +
    "{\"star\":1,\"name\":\"明日奈\"}," +
    "{\"star\":1,\"name\":\"吹雪\"}," +
    "{\"star\":1,\"name\":\"菲娜\"}," +
    "{\"star\":1,\"name\":\"巴\"}," +
    "{\"star\":1,\"name\":\"琴里\"}," +
    "{\"star\":1,\"name\":\"和香\"}," +
    "{\"star\":1,\"name\":\"志美子\"}," +
    "{\"star\":1,\"name\":\"朱莉\"}," +
    "{\"star\":1,\"name\":\"小玉\"}," +
    "{\"star\":1,\"name\":\"千夏\"}," +
    "{\"star\":1,\"name\":\"芹娜\"}," +
    "{\"star\":1,\"name\":\"满\"}]")
//    "{\"star\":1,\"name\":\"鹤城(泳装)\"}")
  val REWARD_LIST_TWO_STAR: JsonElement = Json.parseToJsonElement("[{\"star\":2,\"name\":\"芹香\"}," +
    "{\"star\":2,\"name\":\"睦月\"}," +
    "{\"star\":2,\"name\":\"茜里\"}," +
    "{\"star\":2,\"name\":\"玛丽\"}," +
    "{\"star\":2,\"name\":\"静子\"}," +
    "{\"star\":2,\"name\":\"花江\"}," +
    "{\"star\":2,\"name\":\"风香\"}," +
    "{\"star\":2,\"name\":\"花子\"}," +
    "{\"star\":2,\"name\":\"绫音\"}," +
    "{\"star\":2,\"name\":\"晴\"}," +
    "{\"star\":2,\"name\":\"爱莉\"}," +
    "{\"star\":2,\"name\":\"茜\"}," +
    "{\"star\":2,\"name\":\"桃衣\"}," +
    "{\"star\":2,\"name\":\"纯子\"}," +
    "{\"star\":2,\"name\":\"野宫\"}," +
    "{\"star\":2,\"name\":\"莲见\"}," +
    "{\"star\":2,\"name\":\"千世\"}," +
    "{\"star\":2,\"name\":\"桐乃\"}," +
    "{\"star\":2,\"name\":\"佳代子\"}," +
    "{\"star\":2,\"name\":\"优香\"}," +
    "{\"star\":2,\"name\":\"歌原\"}]")
  val REWARD_LIST_THREE_STAR: JsonElement = Json.parseToJsonElement("[{\"star\":3,\"name\":\"美咲\"}," +
    "{\"star\":3,\"name\":\"日和\"}," +
    "{\"star\":3,\"name\":\"月咏\"}," +
    "{\"star\":3,\"name\":\"美咲\"}," +
    "{\"star\":3,\"name\":\"梓\"}," +
    "{\"star\":3,\"name\":\"瞬\"}," +
    "{\"star\":3,\"name\":\"白子\"}," +
    "{\"star\":3,\"name\":\"泉\"}," +
    "{\"star\":3,\"name\":\"日奈\"}," +
    "{\"star\":3,\"name\":\"阿露\"}," +
    "{\"star\":3,\"name\":\"柚子\"}," +
    "{\"star\":3,\"name\":\"切里诺\"}," +
    "{\"star\":3,\"name\":\"绿\"}," +
    "{\"star\":3,\"name\":\"鹤城\"}," +
    "{\"star\":3,\"name\":\"菫\"}," +
    "{\"star\":3,\"name\":\"尼禄\"}," +
    "{\"star\":3,\"name\":\"真纪\"}," +
    "{\"star\":3,\"name\":\"伊织\"}," +
    "{\"star\":3,\"name\":\"日向\"}," +
    "{\"star\":3,\"name\":\"若藻\"}," +
    "{\"star\":3,\"name\":\"爱丽丝\"}," +
    "{\"star\":3,\"name\":\"泉奈\"}," +
    "{\"star\":3,\"name\":\"晴奈\"}," +
    "{\"star\":3,\"name\":\"真白\"}," +
    "{\"star\":3,\"name\":\"沙绫\"}," +
    "{\"star\":3,\"name\":\"响\"}," +
    "{\"star\":3,\"name\":\"咲\"}," +
    "{\"star\":3,\"name\":\"千寻\"}," +
    "{\"star\":3,\"name\":\"花凛\"}," +
    "{\"star\":3,\"name\":\"艾米\"}," +
    "{\"star\":3,\"name\":\"宫子\"}," +
    "{\"star\":3,\"name\":\"玛丽娜\"}," +
    "{\"star\":3,\"name\":\"星野\"}," +
    "{\"star\":2,\"name\":\"椿\"}," +
    "{\"star\":3,\"name\":\"月咏\"}," +
    "{\"star\":3,\"name\":\"夏\"}," +
    "{\"star\":3,\"name\":\"忧\"}," +
    "{\"star\":3,\"name\":\"美游\"}," +
    "{\"star\":3,\"name\":\"日富美\"}," +
    "{\"star\":3,\"name\":\"三森\"}," +
    "{\"star\":3,\"name\":\"日和\"}," +
    "{\"star\":3,\"name\":\"枫\"}," +
    "{\"star\":3,\"name\":\"亚子\"}," +
    "{\"star\":3,\"name\":\"小春\"}," +
    "{\"star\":3,\"name\":\"伊吕波\"}," +
    "{\"star\":3,\"name\":\"濑名\"}]")
//    "{\"star\":3,\"name\":\"瞬(幼女)\"}," +
//    "{\"star\":3,\"name\":\"伊织(泳装)\"}," +
//    "{\"star\":3,\"name\":\"日奈(泳装)\"}," +
//    "{\"star\":3,\"name\":\"阿露(正月)\"}," +
//    "{\"star\":3,\"name\":\"睦月(正月)\"}," +
//    "{\"star\":3,\"name\":\"花凛(兔女郎)\"}," +
//    "{\"star\":3,\"name\":\"白子(骑行)\"}," +
//    "{\"star\":3,\"name\":\"梓(泳装)\"}," +
//    "{\"star\":3,\"name\":\"沙绫(私服)\"}," +
//    "{\"star\":3,\"name\":\"真白(泳装)\"}," +
//    "{\"star\":3,\"name\":\"尼禄(兔女郎)\"}," +
//    "{\"star\":3,\"name\":\"千夏(温泉)\"}," +
//    "{\"star\":3,\"name\":\"明日奈(兔女郎)\"}," +
//    "{\"star\":3,\"name\":\"和香(温泉)\"}," +
//    "{\"star\":3,\"name\":\"切里诺(温泉)\"}," +
//    "{\"star\":3,\"name\":\"日富美(泳装)\"}," +
//    "{\"star\":3,\"name\":\"初音未来\"}," +
//    "{\"star\":3,\"name\":\"芹香(正月)\"},")
}