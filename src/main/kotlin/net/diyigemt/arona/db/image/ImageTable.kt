package net.diyigemt.arona.db.image

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

object ImageTable: LongIdTable(name = "Image") {
  val name: Column<String> = char("name", 255)
  val path: Column<String> = char("path", 255)
  val hash: Column<String> = char("hash", 255)
}

class ImageTableModel(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<ImageTableModel>(ImageTable)
  var name by ImageTable.name
  var path by ImageTable.path
  var hash by ImageTable.hash
}