package net.diyigemt.arona.db.model.gacha

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object GachaPoolTable: IntIdTable(name = "GachaPools") {
  val name: Column<String> = varchar("name", 50)
}

class GachaPool(id: EntityID<Int>) : IntEntity(id) {
  companion object: IntEntityClass<GachaPool>(GachaPoolTable)
  val name by GachaPoolTable.name
}