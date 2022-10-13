package net.diyigemt.arona.db.announcement

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

/**
 * 保存已发送的公告id
 */
object RemoteActionTable: LongIdTable(name = "RemoteAction") {
  val aid: Column<Long> = long("aid")
}

class RemoteActionModel(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<RemoteActionModel>(RemoteActionTable)
  var aid by RemoteActionTable.aid
}