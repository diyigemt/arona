package net.diyigemt.arona.remote

import kotlin.reflect.KType

interface RemoteService<T> {

  val kType: KType
  val type: RemoteServiceAction
  fun handleService(data: T, time: String, aid: Long)

}

enum class RemoteServiceAction(
  val action: String
) {
  ANNOUNCEMENT("announcement"), // 公告消息
  POOL_UPDATE("poolUpdate"), // 卡池更新消息
  VERSION_UPDATE("versionUpdate"), // 版本更新消息
  NULL("null");
  companion object {
    fun getRemoteServiceActionByName(name: String): RemoteServiceAction =
      RemoteServiceAction.values()
        .firstOrNull { it.action == name }
        ?: NULL
  }
}