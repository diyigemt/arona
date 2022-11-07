package net.diyigemt.arona.db.gacha

import net.diyigemt.arona.annotations.DTOService
import net.diyigemt.arona.db.BaseDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow

@DTOService
object GachaPoolCharactersTable: IntIdTable(name = "GachaPoolCharacters") {
  val poolId = reference("pool_id", GachaPoolsTable.id)
  val characterId = reference("character_id", GachaCharactersTable.id)

  data class GachaPoolCharactersDTO(
    val poolId: Int = 0,
    val characterId: Int = 0
  ): BaseDTO<GachaPoolCharactersDTO>{
    override fun toModel(results: List<ResultRow>): List<GachaPoolCharactersDTO> {
      val res: MutableList<GachaPoolCharactersDTO> = mutableListOf()

      results.forEach {
        res.add(
          GachaPoolCharactersDTO(
            it[GachaPoolCharactersTable.poolId].value,
            it[GachaPoolCharactersTable.characterId].value,
          )
        )
      }

      return res
    }
  }
}

class GachaPoolCharacters(id: EntityID<Int>) : IntEntity(id) {
  companion object: IntEntityClass<GachaPoolCharacters>(GachaPoolCharactersTable)
  var poolId by GachaPoolCharactersTable.poolId
  var characterId by GachaPoolCharactersTable.characterId
}
