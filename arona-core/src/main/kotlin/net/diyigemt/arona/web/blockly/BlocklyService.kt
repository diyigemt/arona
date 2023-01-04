package net.diyigemt.arona.web.blockly

import net.diyigemt.arona.Arona
import net.diyigemt.arona.interfaces.Initialize

/**
 *@Author hjn
 *@Create 2022/12/26
 */
object BlocklyService: Initialize {
  private val hooks: MutableList<BlocklyExpression> = mutableListOf()

  override fun init() {
    //TODO("Not yet implemented")
  }

  fun checkAndDeserialize(data: String): CommitData? = kotlin.runCatching {
    BlocklyInterpreter.deserialize(data)
  }.getOrElse {
    it.printStackTrace()
    return null
  }

  fun trigger() = Arona.async {
    for(hook in hooks){
      Arona.info("Begin")
      val expression = BlocklyInterpreter.generateBooleanExpression(hook, null)
      Arona.info(expression)
      //TODO:ID判断
      if (BlocklyInterpreter.evaluateAsBoolean(expression) == true){
        for (action in hook.actions){
          action.id.run(action.value, null)
        }
      }
    }
  }

  fun addHook(data: CommitData): Boolean = SaveManager.addSaveFromRemote(data).apply {
    if (this) {
      hooks.add(data.trigger)
      Arona.info("触发器: ${data.projectName}, 添加成功")
    }
  }

  fun addHook(data: BlocklyExpression) = hooks.add(data)
}