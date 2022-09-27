package net.diyigemt.arona.remote

import kotlin.reflect.KType


interface RemoteService<T> {

  val kType: KType
  fun handleService(data: T)

}

enum class RemoteServiceAction(
  private val action: String
) {
  ANNOUNCEMENT("announcement"),
  NULL("null");
  companion object {
    fun getRemoteServiceActionByName(name: String): RemoteServiceAction =
      RemoteServiceAction.values()
        .firstOrNull { it.action == name }
        ?: NULL
  }
}