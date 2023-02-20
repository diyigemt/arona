package net.diyigemt.arona.db.reply

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

/**
 *@Author hjn
 *@Create 2023/2/19
 */
object Labels: IntIdTable("Labels") {
  val value: Column<String> = varchar("tagName", 255)
  val weight: Column<Int> = integer("weight")
}

@Serializable
data class LabelItem(
  val id: Int,
  val value: String,
  val weight: Int,
)