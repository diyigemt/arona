package net.diyigemt.arona.web.blockly

import net.diyigemt.arona.util.MoshiUtil
import net.mamoe.mirai.event.events.MessageEvent
import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.MapContext

/**
 *@Author hjn
 *@Create 2022/12/24
 */
object BlocklyInterpreter {
  private val jexlEngine = JexlBuilder().create()
  private val jexlContext = MapContext()

  fun generateBooleanExpression(value: BlocklyTrigger, event: MessageEvent?): String {
    var res = ""
    kotlin.runCatching {
      if(value.expressions.isEmpty()) res = "true"
      else for (item in value.expressions) {
        res += when(item.id){
          DataTypes.AND -> "&&"
          DataTypes.OR -> "||"
          DataTypes.NOT -> "!"
          DataTypes.TRUE -> "true"
          DataTypes.FALSE -> "false"

          else -> item.id.run(item.value, event).toString()
        }
      }
    }.onFailure {
      it.printStackTrace()
    }

    return res
  }

  private fun evaluate(expression: String): Any? = kotlin.runCatching {
    jexlEngine.createExpression(expression).evaluate(jexlContext)
  }.getOrElse {
    it.printStackTrace()
    //Arona.error("Can't evaluate expression: $expression")
    return@getOrElse null
  }

  fun evaluateAsBoolean(expression: String): Boolean? = kotlin.runCatching {
    evaluate(expression) as? Boolean
  }.getOrElse {
    it.printStackTrace()
    return@getOrElse null
  }

  fun deserialize(value: String): CommitData = MoshiUtil.custom(UUIDAdapter()).adapter(CommitData::class.java).fromJson(value)!!
}