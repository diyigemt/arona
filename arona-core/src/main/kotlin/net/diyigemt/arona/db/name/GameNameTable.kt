package net.diyigemt.arona.db.name

import net.diyigemt.arona.annotations.DTOService
import net.diyigemt.arona.db.BaseDTO
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

@DTOService
object GameNameTable: IdTable<Long>(name = "GameName") {
  override val id: Column<EntityID<Long>> = long("qq").entityId()
  val name: Column<String> = char("name", 50)

  override val primaryKey: PrimaryKey = PrimaryKey(id)

  data class GameNameDTO(
    val id: Long = 0,
    val name: String = ""
  ): BaseDTO<GameNameDTO>{
    override fun toModel(results: List<ResultRow>): List<GameNameDTO> {
      val res: MutableList<GameNameDTO> = mutableListOf()

      results.forEach {
        res.add(
          GameNameDTO(
            it[GameNameTable.id].value,
            it[GameNameTable.name]
          )
        )
      }

      return res
    }
  }
}

class GameName(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<GameName>(GameNameTable)
  var name by GameNameTable.name
}