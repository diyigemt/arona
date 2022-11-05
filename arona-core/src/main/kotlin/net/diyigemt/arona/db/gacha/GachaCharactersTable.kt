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
object GachaCharactersTable: IntIdTable(name = "GachaCharacters") {
  val name: Column<String> = varchar("name", 10)
  val star: Column<Int> = integer("star")
  val limit: Column<Boolean> = bool("limit") // 区分常驻和限定

  data class GachaCharactersDTO(
    val name: String = "",
    val star: Int = 0,
    val limit: Boolean = false
  ): BaseDTO<GachaCharactersDTO>{
    override fun toModel(results: List<ResultRow>): List<GachaCharactersDTO> {
      val res: MutableList<GachaCharactersDTO> = mutableListOf()
      results.forEach {
        res.add(GachaCharactersDTO(
          it[GachaCharactersTable.name],
          it[GachaCharactersTable.star],
          it[GachaCharactersTable.limit]
        ))
      }

      return res
    }
  }
}

class GachaCharacters(id: EntityID<Int>) : IntEntity(id) {
  companion object: IntEntityClass<GachaCharacters>(GachaCharactersTable)

  var name by GachaCharactersTable.name
  var star by GachaCharactersTable.star
  var limit by GachaCharactersTable.limit
}
