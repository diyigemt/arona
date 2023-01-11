package net.diyigemt.arona.web.blockly

import kotlinx.serialization.Serializable
import net.diyigemt.arona.Arona
import net.mamoe.mirai.console.util.safeCast
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import java.util.*

/**
 *@Author hjn
 *@Create 2022/12/23
 */
enum class DataTypes: AronaInterceptor{
  AND {
    override fun run(value: Any, event: MessageEvent?): Boolean {
      TODO("不要实现，实现了也没用")
    }
  },
  OR {
    override fun run(value: Any, event: MessageEvent?): Boolean {
      TODO("不要实现，实现了也没用")
    }
  },
  NOT {
    override fun run(value: Any, event: MessageEvent?): Boolean {
      TODO("不要实现，实现了也没用")
    }
  },
  TRUE {
    override fun run(value: Any, event: MessageEvent?): Boolean {
      TODO("测试用，会直接返回对应的布尔值，不要实现，实现了也没用")
    }
  },
  FALSE {
    override fun run(value: Any, event: MessageEvent?): Boolean {
      TODO("测试用，会直接返回对应的布尔值，不要实现，实现了也没用")
    }
  },
  // TODO: 日后支持其它种类消息需要调整参数, 对123456L仅支持DEV，在正式发布之前需要删掉
  SENDER {
    override fun run(value: Any, event: MessageEvent?): Boolean {
      val tmp = value as? Double ?: kotlin.run {
        Arona.error("ID: Convert $value to Double failed")
        return false
      }
      if(tmp.toLong() == 123456L || tmp.toLong() == event?.sender?.id) return true

      return false
    }
  },
  ID {
    override fun run(value: Any, event: MessageEvent?): Boolean {
      val tmp = value as? Double ?: kotlin.run {
        Arona.error("ID: Convert $value to Double failed")
        return false
      }
      val resEvent = event.safeCast<GroupMessageEvent>() ?: kotlin.run {
        Arona.error("ID: Convert ${event?.javaClass?.name} to GroupMessageEvent failed")
        return false
      }
      if(tmp.toLong() == 123456L || tmp.toLong() == resEvent.group.id) return true

      return false
    }
  }
}

enum class ActionTypes: Runner{
  SEND_MSG {
    override fun run(values: List<Any>, event: MessageEvent?) {
      Arona.info(values[0].toString())
      // Arona.sendMessage(values[0].toString())
      // TODO: sendMessage好使了记得加上然后把下面删了
      Arona.runSuspend {
        event?.subject?.sendMessage(values[0].toString())
      }
    }
  }
}

enum class EventType{
  GroupMessageEvent,
  FriendMessageEvent
}

interface AronaInterceptor{
  fun run(value: Any, event: MessageEvent?): Boolean
}

interface Runner{
  fun run(values: List<Any>, event: MessageEvent?)
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
  val type: EventType,
  val expressions: List<IfExpression>,
  val actions: List<Actions>
)

data class CommitData(
  val mode: String,
  val trigger: BlocklyExpression,
  val projectName: String,
  val uuid: UUID?,
  val blocklyProject: String,
  val userData: String
)

@Serializable
data class ListSaves(
  val name: String,
  @Serializable(with = UUIDSerializer::class)
  val uuid: UUID,
  val blocklyProject: String
)