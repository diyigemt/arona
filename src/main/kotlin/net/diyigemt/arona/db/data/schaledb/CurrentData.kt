package net.diyigemt.arona.db.data.schaledb

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

/**
 *@Author hjn
 *@Create 2022/8/28
 */

object CurrentData : Table("CurrentData") {
  val value : Column<Int> = integer("value")
  val type : Column<String> = varchar("type", 10)
  val server : Column<String> = varchar("server", 10)
  val start : Column<Long> = long("start")
  val end : Column<Long> = long("end")
}