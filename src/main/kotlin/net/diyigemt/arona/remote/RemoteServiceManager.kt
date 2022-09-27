package net.diyigemt.arona.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import net.diyigemt.arona.Arona
import net.diyigemt.arona.advance.RemoteActionItem
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.announcement.RemoteActionModel
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.remote.action.AnnouncementRemoteService
import net.diyigemt.arona.remote.action.GachaPoolUpdateRemoteService
import kotlin.reflect.full.createType

object RemoteServiceManager: InitializedFunction() {

  private val MAP: MutableMap<RemoteServiceAction, RemoteService<Any>> = mutableMapOf()
  private val JsonObject: Json = Json { ignoreUnknownKeys = true }

  fun dispatchService(actionList: List<RemoteActionItem>) {
    actionList.forEach {
      val action = RemoteServiceAction.getRemoteServiceActionByName(it.action)
      MAP[action]?.also { rs ->
        kotlin.runCatching {
          if (rs.kType == String::class.createType()) {
            return@runCatching it.content
          }
          JsonObject.decodeFromString(serializer(rs.kType), it.content)
        }.onSuccess { json ->
          rs.handleService(json!!, it.time)
          DataBaseProvider.query { _ ->
            RemoteActionModel.new {
              this.aid = it.id
            }
          }
        }.onFailure { err ->
          err.printStackTrace()
        }
      }
    }
  }

  fun registerService(type: RemoteServiceAction, self: RemoteService<Any>) {
    if (MAP[type] != null) {
      Arona.warning("duplicated type: ${type.name}")
    }
    MAP[type] = self
  }

  override fun init() {
    AnnouncementRemoteService().init()
    GachaPoolUpdateRemoteService().init()
  }

}