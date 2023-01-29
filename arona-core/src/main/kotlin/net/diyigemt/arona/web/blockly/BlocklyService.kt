package net.diyigemt.arona.web.blockly

import net.diyigemt.arona.Arona
import net.diyigemt.arona.interfaces.Initialize
import net.diyigemt.arona.service.AronaMessageReactService
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import java.util.*

/**
 *@Author hjn
 *@Create 2022/12/26
 */
object BlocklyService: Initialize, AronaMessageReactService<MessageEvent> {
  private val groupHooks: MutableMap<UUID, List<BlocklyTrigger>> = mutableMapOf()
  private val friendHooks: MutableMap<UUID, List<BlocklyTrigger>> = mutableMapOf()

  override fun init() {
    SaveManager.init()
  }

  override suspend fun handle(event: MessageEvent) = when(event) {
    is GroupMessageEvent -> optionalTrigger(groupHooks.values, event)
    is FriendMessageEvent -> optionalTrigger(friendHooks.values, event)

    else -> Arona.error("Undefined event type: ${event.javaClass.name}")
  }

  fun checkAndDeserialize(data: String): CommitData? = kotlin.runCatching {
    BlocklyInterpreter.deserialize(data)
  }.getOrElse {
    it.printStackTrace()
    return null
  }

  /**
   * 测试用，会遍历全部的触发器*/
  fun trigger() = Arona.async {
    val hooks = friendHooks + groupHooks
    for(hook in hooks){
      Arona.info("Begin")
      hook.value.forEach {
        val expression = BlocklyInterpreter.generateBooleanExpression(it, null)
        Arona.info(expression)
        if (BlocklyInterpreter.evaluateAsBoolean(expression) == true){
          for (action in it.actions) {
            action.id.run(action.value, null)
          }
        }
      }
    }
  }

  private fun optionalTrigger(hooks: Collection<List<BlocklyTrigger>>, event: MessageEvent) {
    hooks.forEach {
      it.forEach { trigger ->
        val expression = BlocklyInterpreter.generateBooleanExpression(trigger, event)
        Arona.info(expression)
        if (BlocklyInterpreter.evaluateAsBoolean(expression) == true) {
          trigger.actions.forEach { actions ->
            actions.id.run(actions.value, event)
          }
        }
      }
    }
  }

  fun updateTriggers(uuid: UUID, data: BlocklyExpression, isDelete: Boolean = false) {
    if(isDelete) {
      when(data.type) {
        EventType.GroupMessageEvent -> groupHooks.remove(uuid)
        EventType.FriendMessageEvent -> friendHooks.remove(uuid)

        else -> Arona.error("Undefined event type: ${data.type}")
      }
    } else {
      when(data.type) {
        EventType.GroupMessageEvent -> groupHooks[uuid] = data.triggers
        EventType.FriendMessageEvent -> friendHooks[uuid] = data.triggers

        else -> Arona.error("Undefined event type: ${data.type}")
      }
    }
  }

  fun addHook(data: CommitData): Boolean {
    SaveManager.addSaveFromRemote(data).apply {
      if (this != null) {
        updateTriggers(this, data.trigger)
        Arona.info("触发器: ${data.projectName}, 添加成功")

        return true
      }
    }

    return false
  }

  fun updateHook(data: CommitData): Boolean {
    if (data.uuid == null) return false
    SaveManager.updateSaveFromRemote(data).apply {
      if (this){
        updateTriggers(data.uuid, data.trigger)
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
    val meta = SaveManager.getSaveElementByUUID<Meta>(data.uuid) ?: return -2
    SaveManager.deleteSaveFormRemote(data).apply {
      if(this) {
        updateTriggers(data.uuid, data.trigger, true)
        Arona.info("触发器: ${meta.projectName}, 删除成功")

        return 0
      }
    }

    return -3
  }

  override val event = MessageEvent::class
  override val id = 26
  override val name = "blockly项目配置服务"
  override val description = "blockly项目配置服务"
  override var isGlobal = true
}