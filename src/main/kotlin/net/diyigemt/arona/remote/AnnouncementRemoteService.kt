package net.diyigemt.arona.remote

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.reflect.KType
import kotlin.reflect.full.createType

@kotlinx.serialization.Serializable
data class AnnouncementItem(
  val id: Long,
  val content: String,
  val time: String
)

class AnnouncementRemoteService : RemoteService<AnnouncementItem> {

  override val kType: KType = AnnouncementItem::class.createType()
  override fun handleService(data: AnnouncementItem) {
  }
}