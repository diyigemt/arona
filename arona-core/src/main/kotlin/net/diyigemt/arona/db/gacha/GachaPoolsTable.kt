package net.diyigemt.arona.db.gacha

import net.diyigemt.arona.annotations.DTOService
import net.diyigemt.arona.db.BaseDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

@DTOService
object GachaPoolsTable: IntIdTable(name = "GachaPools") {
  val name: Column<String> = varchar("name", 50)

  data class GachaPoolsDTO(
    val id: Int = 0,
    val name: String = ""
  ): BaseDTO<GachaPoolsDTO>{
    override fun toModel(results: List<ResultRow>): List<GachaPoolsDTO> {
      val res: MutableList<GachaPoolsDTO> = mutableListOf()
      results.forEach{
        res.add(
          GachaPoolsDTO(
            it[GachaPoolsTable.id].value,
            it[GachaPoolsTable.name]
          )
        )
      }
      return res
    }
  }
}

class GachaPools(id: EntityID<Int>) : IntEntity(id) {
  companion object: IntEntityClass<GachaPools>(GachaPoolsTable)
  var name by GachaPoolsTable.name
}