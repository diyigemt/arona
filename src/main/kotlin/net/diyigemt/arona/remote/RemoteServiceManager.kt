package net.diyigemt.arona.remote

object RemoteServiceManager {

  private val MAP: MutableMap<RemoteServiceAction, MutableList<RemoteService>> = mutableMapOf()

  fun dispatchService(action: String, params: String) {
    val actionList = RemoteServiceAction.getRemoteServiceActionByName(action)
    MAP[actionList]?.forEach {
      it.handleService(params)
    }
  }

}