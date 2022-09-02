package net.diyigemt.arona.db.name

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object GameNameTable: IdTable<Long>(name = "GameName") {
  override val id: Column<EntityID<Long>> = long("qq").entityId()
  val name: Column<String> = char("name", 50)

  override val primaryKey: PrimaryKey = PrimaryKey(id)
}

class GameName(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<GameName>(GameNameTable)
  var name by GameNameTable.name
}