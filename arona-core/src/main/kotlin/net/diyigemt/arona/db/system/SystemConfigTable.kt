package net.diyigemt.arona.db.system

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

object SystemConfigTable: LongIdTable(name = "SystemConfig") {
  val key: Column<String> = char("key", 255)
  val value: Column<String> = char("value", 255)
}

class SystemConfigTableModel(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<SystemConfigTableModel>(SystemConfigTable)
  var key by SystemConfigTable.key
  var value by SystemConfigTable.value
}