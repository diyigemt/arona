package net.diyigemt.arona.db.data.schaledb

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

/**
 *@Author hjn
 *@Create 2022/8/28
 */
object Raid : Table("Raid") {
  val Id : Column<Int> = integer("Id")
  val IsReleased : Column<Boolean> = bool("IsReleased")
  val NameCn : Column<String> = varchar("NameCn", 20)
}