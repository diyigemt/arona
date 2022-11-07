package net.diyigemt.arona.remote.action

import kotlinx.serialization.Serializable
import net.diyigemt.arona.Arona
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.gacha.GachaPools
import net.diyigemt.arona.db.gacha.GachaPoolsTable
import net.diyigemt.arona.remote.RemoteService
import net.diyigemt.arona.remote.RemoteServiceAction
import net.diyigemt.arona.util.GachaUtil
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class GachaPoolUpdateRemoteService : RemoteService<GachaPoolUpdateData> {
  //  override val kType: KType = List::class.createType(listOf(KTypeProjection.invariant(AnnouncementItem::class.starProjectedType)))
  override val kType: KType = GachaPoolUpdateData::class.createType()
  override val type: RemoteServiceAction = RemoteServiceAction.POOL_UPDATE

  override fun handleService(data: GachaPoolUpdateData, time: String, aid: Long) {
    // 检测是否有池子名称冲突
    val pool = DataBaseProvider.query {
      GachaPools.find { GachaPoolsTable.name eq data.name }.firstOrNull()
    }
    val crash = pool != null
    val message = """
      检测到新池子: ${data.name} ${if (crash) "(与现有池子名称冲突)" else ""}
      ${data.character.joinToString(", ") { GachaUtil.mapStudentInfo(it.name, it.star) }}
      使用指令
      /gacha ${if (crash) "update2" else "update"} $aid ${if (crash) "新池子名称" else ""}
      来更新这个池子
    """.trimIndent()
    Arona.sendMessageToAdmin(message)
  }
}

@Serializable
data class GachaCharacter(
  val name: String,
  val star: Int,
  val limit: Int
)

@Serializable
data class GachaPoolUpdateData(
  val name: String,
  val character: List<GachaCharacter>
)