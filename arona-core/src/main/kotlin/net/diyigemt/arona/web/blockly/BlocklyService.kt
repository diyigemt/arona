package net.diyigemt.arona.web.blockly

import net.diyigemt.arona.Arona
import net.diyigemt.arona.interfaces.Initialize
import java.util.*

/**
 *@Author hjn
 *@Create 2022/12/26
 */
object BlocklyService: Initialize {
  private val hooks: MutableMap<UUID, BlocklyExpression> = mutableMapOf()

  override fun init() {
    SaveManager.init()
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
      val expression = BlocklyInterpreter.generateBooleanExpression(hook.value, null)
      Arona.info(expression)
      //TODO:ID判断
      if (BlocklyInterpreter.evaluateAsBoolean(expression) == true){
        for (action in hook.value.actions){
          action.id.run(action.value, null)
        }
      }
    }
  }

  fun addHook(data: CommitData): Boolean {
    SaveManager.addSaveFromRemote(data).apply {
      if (this != null) {
        hooks[this] = data.trigger
        Arona.info("触发器: ${data.projectName}, 添加成功")

        return true
      }
    }

    return false
  }

  fun addHook(uuid: UUID, data: BlocklyExpression) = hooks.put(uuid, data)

  fun updateHook(data: CommitData): Boolean {
    if (data.uuid == null) return false
    SaveManager.updateSaveFromRemote(data).apply {
      if (this){
        hooks[data.uuid] = data.trigger
        Arona.info("触发器: ${data.projectName}, 修改成功")

        return true
      }
    }

    return false
  }

  /**
   * @return
   * -1 UUID为空
   * -2 获取存档元数据失败
   * -3 程序BUG
   * */
  fun deleteHook(data: CommitData): Int {
    if(data.uuid == null) return -1
    val meta = SaveManager.getMetaByUUID(data.uuid) ?: return -2
    SaveManager.deleteSaveFormRemote(data).apply {
      if(this) {
        hooks.remove(data.uuid)
        Arona.info("触发器: ${meta.projectName}, 删除成功")

        return 0
      }
    }

    return -3
  }
}