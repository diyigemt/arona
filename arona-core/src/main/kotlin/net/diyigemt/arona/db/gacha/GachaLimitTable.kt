package net.diyigemt.arona.db.gacha

import net.diyigemt.arona.annotations.DTOService
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.db.BaseDTO
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.util.TimeUtil
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.update

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

  fun update() {
    val today = TimeUtil.today()
    if (AronaGachaConfig.day == today) return
    AronaGachaConfig.day = today
    forceUpdate()
  }

  fun forceUpdate(group0: Long? = null) {
    DataBaseProvider.query {
      if (group0 == null) {
        GachaLimitTable.update() {
          it[count] = 0
        }
      } else {
        GachaLimitTable.update({ group eq group0 }) {
          it[count] = 0
        }
      }
    }
  }
}

class GachaLimit(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<GachaLimit>(GachaLimitTable)
  var group by GachaLimitTable.group
  var count by GachaLimitTable.count
}