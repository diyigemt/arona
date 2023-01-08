package net.diyigemt.arona.db.data.schaledb

import net.diyigemt.arona.annotations.SkipSchaleDbInit
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

/**
 *@Author hjn
 *@Create 2022/8/24
 */
@SkipSchaleDbInit
object Students : Table("Students") {
  val studentID: Column<Int> = integer("studentID")
  val name: Column<String> = varchar("name", 15)
  val birthday: Column<String> = varchar("birthday", 5)

  override val primaryKey: PrimaryKey = PrimaryKey(studentID)
}