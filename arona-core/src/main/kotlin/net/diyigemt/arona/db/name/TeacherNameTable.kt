package net.diyigemt.arona.db.name

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object TeacherNameTable: IdTable<Long>(name = "TeacherName") {
  override val id: Column<EntityID<Long>> = long("qq").entityId()
  val group: Column<Long> = long("group")
  val name: Column<String> = char("name", 20)

  override val primaryKey: PrimaryKey = PrimaryKey(id, group)
}

class TeacherName(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<TeacherName>(TeacherNameTable)
  var group by TeacherNameTable.group
  var name by TeacherNameTable.name
}