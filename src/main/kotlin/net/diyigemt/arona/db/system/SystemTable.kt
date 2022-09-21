package net.diyigemt.arona.db.system

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

object SystemTable: LongIdTable(name = "System") {
  val key: Column<String> = char("key", 255)
  val value: Column<String> = char("value", 255)
}

class SystemTableModel(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<SystemTableModel>(SystemTable)
  var key by SystemTable.key
  var value by SystemTable.value
}