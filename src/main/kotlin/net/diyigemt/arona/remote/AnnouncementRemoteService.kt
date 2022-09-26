package net.diyigemt.arona.remote

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@kotlinx.serialization.Serializable
data class AnnouncementItem(
  val id: Long,
  val content: String,
  val time: String
)

class AnnouncementRemoteService: RemoteService {

  override fun handleService(data: String) {
    Json.decodeFromString<AnnouncementItem>(data)
  }
}