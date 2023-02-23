package net.diyigemt.arona.db.reply

import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

/**
 *@Author hjn
 *@Create 2023/2/21
 */
object MessageGroups: IntIdTable("MessageGroups") {
  val content: Column<String> = varchar("content", 1000000)
  val weight: Column<Int> = integer("weight")
  val labels: Column<String> = varchar("labels", 255)
}

@Serializable
data class MessageGroup(
  val id: Int,
  val content: List<ContentItem>,
  val weight: Int,
  val label: List<Int>
)

@Serializable
@JsonClass(generateAdapter = true)
data class ContentItem(
  val type: MessageKind,
  val content: String
)

enum class MessageKind {
  String, Image
}