package net.diyigemt.arona.db.tarot

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object TarotRecordTable: IdTable<Long>(name = "TarotRecord") {
  override val id: Column<EntityID<Long>> = long("qq").entityId()
  val group: Column<Long> = long("group")
  val day: Column<Int> = integer("day")

  override val primaryKey: PrimaryKey = PrimaryKey(id, group)
}

class TarotRecord(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<TarotRecord>(TarotRecordTable)
  var group by TarotRecordTable.group
  var day by TarotRecordTable.day
}