package net.diyigemt.arona.db.gacha

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object GachaHistoryTable: IdTable<Long>(name = "GachaHistory") {
  override val id: Column<EntityID<Long>> = long("qq").entityId()
  val group: Column<Long> = long("group")
  val pool: Column<Int> = integer("pool")
  val points: Column<Int> = integer("points")
  val count3: Column<Int> = integer("count3")
  val dog: Column<Int> = integer("dog")

  override val primaryKey: PrimaryKey = PrimaryKey(id, group, pool)
}

class GachaHistory(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<GachaHistory>(GachaHistoryTable)
  var group by GachaHistoryTable.group
  var pool by GachaHistoryTable.pool
  var points by GachaHistoryTable.points
  var count3 by GachaHistoryTable.count3
  var dog by GachaHistoryTable.dog
}