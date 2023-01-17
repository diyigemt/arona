package net.diyigemt.arona.web.blockly

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.diyigemt.arona.Arona
import net.diyigemt.arona.web.entity.BotFriend
import net.diyigemt.arona.web.entity.BotGroupMember
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.console.util.SemVersion
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import java.util.*

/**
 *@Author hjn
 *@Create 2023/1/3
 * 当2.0.0之后新加变量必须使用可空类型，为了版本兼容
 */
interface SaveDataElement

@Serializable
data class Meta(
  val version: SemVersion = Arona.version,
  val projectName: String = "Untiled",
  val resPath: String = "",
  @Serializable(with = UUIDSerializer::class)
  val uuid: UUID = UUID.randomUUID()
): SaveDataElement


@Serializable
data class UserData(
  var friends: List<BotFriend>,
  var members: List<Members>,
): SaveDataElement


@Serializable
data class Members(
  val groupId: String = "",
  var members: List<BotGroupMember> = mutableListOf()
)



object UUIDSerializer: KSerializer<UUID>{
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
  override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
  override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}

class UUIDAdapter {
  @ToJson
  fun toJson(uuid: UUID): String = uuid.toString()

  @FromJson
  fun fromJson(uuid: String): UUID = UUID.fromString(uuid)
}