package net.diyigemt.arona.db.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object GachaCharacterTable: IntIdTable() {
  val name: Column<String> = varchar("name", 10)
  val star: Column<Int> = integer("star")
  val limit: Column<Boolean> = bool("limit")
  val pickup: Column<Boolean> = bool("pickup")
}

class GachaCharacter(id: EntityID<Int>): IntEntity(id) {
  companion object:IntEntityClass<GachaCharacter>(GachaCharacterTable)
  val name by GachaCharacterTable.name
  val star by GachaCharacterTable.star
  val limit by GachaCharacterTable.limit
  val pickup by GachaCharacterTable.pickup

}