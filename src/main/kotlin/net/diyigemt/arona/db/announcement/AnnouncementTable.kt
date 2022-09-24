package net.diyigemt.arona.db.announcement

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

/**
 * 保存已发送的公告id
 */
object AnnouncementTable: LongIdTable(name = "Announcement") {
  val aid: Column<Long> = long("aid")
}

class AnnouncementModel(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<AnnouncementModel>(AnnouncementTable)
  var aid by AnnouncementTable.aid
}