package net.diyigemt.arona.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

object RemoteServiceManager {

  private val MAP: MutableMap<RemoteServiceAction, RemoteService<Any>> = mutableMapOf()
  private val json: Json = Json { ignoreUnknownKeys = true }

  fun dispatchService(action: String, params: String) {
    val actionList = RemoteServiceAction.getRemoteServiceActionByName(action)
    MAP[actionList]?.also {
      json.decodeFromString(serializer(it.kType), params)?.let { it1 -> it.handleService(it1) }
    }
  }

}