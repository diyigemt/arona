package net.diyigemt.arona.web.blockly

import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable
import net.diyigemt.arona.Arona
import net.mamoe.mirai.event.events.GroupMessageEvent
import java.util.*

/**
 *@Author hjn
 *@Create 2022/12/23
 */
enum class DataTypes: AronaInterceptor{
  AND {
    override fun run(value: Any, event: GroupMessageEvent?): Boolean {
      TODO("不要实现，实现了也没用")
    }
  },
  OR {
    override fun run(value: Any, event: GroupMessageEvent?): Boolean {
      TODO("不要实现，实现了也没用")
    }
  },
  NOT {
    override fun run(value: Any, event: GroupMessageEvent?): Boolean {
      TODO("不要实现，实现了也没用")
    }
  },
  TRUE {
    override fun run(value: Any, event: GroupMessageEvent?): Boolean {
      TODO("测试用，会直接返回对应的布尔值，不要实现，实现了也没用")
    }
  },
  FALSE {
    override fun run(value: Any, event: GroupMessageEvent?): Boolean {
      TODO("测试用，会直接返回对应的布尔值，不要实现，实现了也没用")
    }
  },
  SENDER {
    override fun run(value: Any, event: GroupMessageEvent?): Boolean {
      val tmp = value as? Double ?: kotlin.run {
        Arona.error("ID: Convert $value to Double failed")
        return false
      }
      if(tmp.toLong() == 123456L) return true

      return false
    }
  },
  ID {
    override fun run(value: Any, event: GroupMessageEvent?): Boolean {
      val tmp = value as? Double ?: kotlin.run {
        Arona.error("ID: Convert $value to Double failed")
        return false
      }
      if(tmp.toLong() == 123456L) return true

      return false
    }
  }
}

enum class ActionTypes: Runner{
  SEND_MSG {
    override fun run(values: List<Any>, event: GroupMessageEvent?) {
      Arona.info(values[0].toString())
    }
  }
}

interface AronaInterceptor{
  fun run(value: Any, event: GroupMessageEvent?): Boolean
}

interface Runner{
  fun run(values: List<Any>, event: GroupMessageEvent?)
}

data class IfExpression(
  val id: DataTypes,
  val value: Any
)

data class Actions(
  val id: ActionTypes,
  val value: List<Any>
)

data class BlocklyExpression(
  val id: Long,
  val expressions: List<IfExpression>,
  val actions: List<Actions>
)

data class CommitData(
  val mode: String,
  val trigger: BlocklyExpression,
  val projectName: String,
  val uuid: UUID?,
  val blocklyProject: String,
)

@Serializable
data class ListSaves(
  val name: String,
  @Serializable(with = UUIDSerializer::class)
  val uuid: UUID,
  val blocklyProject: String
)