package net.diyigemt.arona.db.data.schaledb

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

/**
 *@Author hjn
 *@Create 2022/8/28
 */
object MD5 : Table("MD5"){
  val name : Column<String> = varchar("name", 20)
  val remote : Column<String> = varchar("source", 10)
  val md5 : Column<String> = varchar("md5", 32)
}