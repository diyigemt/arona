package net.diyigemt.arona.remote


interface RemoteService {

  fun handleService(data: String)

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