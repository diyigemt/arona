package net.diyigemt.arona.db.data.schaledb

import net.diyigemt.arona.annotations.SkipSchaleDbInit
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

/**
 *@Author hjn
 *@Create 2022/8/28
 */
@SkipSchaleDbInit
object Localization : Table("Localization"){
  val tag : Column<String> = varchar("tag", 10)
  val eventID : Column<Int> = integer("eventID")
  val value : Column<String> = varchar("value", 100)
}