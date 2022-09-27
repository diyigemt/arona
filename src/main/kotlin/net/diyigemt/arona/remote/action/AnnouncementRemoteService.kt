package net.diyigemt.arona.remote.action

import net.diyigemt.arona.Arona
import net.diyigemt.arona.remote.RemoteService
import net.diyigemt.arona.remote.RemoteServiceAction
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class AnnouncementRemoteService : RemoteService<String> {
//  override val kType: KType = List::class.createType(listOf(KTypeProjection.invariant(AnnouncementItem::class.starProjectedType)))
  override val kType: KType = String::class.createType()
  override val type: RemoteServiceAction = RemoteServiceAction.ANNOUNCEMENT

  override fun handleService(data: String, time: String) {
    Arona.sendMessageToAdmin("新公告(${time}):\n${data}")
  }
}