package net.diyigemt.arona.db.gacha

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object GachaCharacterTable: IntIdTable(name = "GachaCharacters") {
  val name: Column<String> = varchar("name", 10)
  val star: Column<Int> = integer("star")
  val limit: Column<Boolean> = bool("limit") // 区分常驻和限定

}

class GachaCharacter(id: EntityID<Int>) : IntEntity(id) {
  companion object: IntEntityClass<GachaCharacter>(GachaCharacterTable)

  var name by GachaCharacterTable.name
  var star by GachaCharacterTable.star
  var limit by GachaCharacterTable.limit
}
