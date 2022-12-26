package net.diyigemt.arona.db.gacha

import net.diyigemt.arona.annotations.DTOService
import net.diyigemt.arona.db.BaseDTO
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

@DTOService
object GachaLimitTable: IdTable<Long>(name = "GachaLimit") {
  override val id: Column<EntityID<Long>> = long("qq").entityId()
  val group: Column<Long> = long("group")
  val count: Column<Int> = integer("count")

  override val primaryKey: PrimaryKey = PrimaryKey(id, group)

  data class GachaLimitDTO(
    val group: Long = 0,
    val count: Int = 0
  ): BaseDTO<GachaLimitDTO>{
    override fun toModel(results: List<ResultRow>): List<GachaLimitDTO> {
      val res: MutableList<GachaLimitDTO> = mutableListOf()
      results.forEach {
        res.add(
          GachaLimitDTO(
            it[GachaLimitTable.group],
            it[GachaLimitTable.count]
          )
        )
      }

      return res
    }
  }
}

class GachaLimit(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<GachaLimit>(GachaLimitTable)
  var group by GachaLimitTable.group
  var count by GachaLimitTable.count
}