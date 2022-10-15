package net.diyigemt.arona.db.gacha

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object GachaPoolCharacterTable: IntIdTable(name = "GachaPoolCharacters") {
  val poolId = reference("pool_id", GachaPoolTable.id)
  val characterId = reference("character_id", GachaCharacterTable.id)
}

class GachaPoolCharacter(id: EntityID<Int>) : IntEntity(id) {
  companion object: IntEntityClass<GachaPoolCharacter>(GachaPoolCharacterTable)
  var poolId by GachaPoolCharacterTable.poolId
  var characterId by GachaPoolCharacterTable.characterId
}
