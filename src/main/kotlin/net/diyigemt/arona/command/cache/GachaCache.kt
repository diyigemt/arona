package net.diyigemt.arona.command.cache
//
import net.diyigemt.arona.Arona
import net.diyigemt.arona.db.model.gacha.GachaCharacter
import net.diyigemt.arona.db.model.gacha.GachaCharacterTable
import net.diyigemt.arona.db.model.gacha.GachaPoolCharacterTable
import net.diyigemt.arona.db.model.gacha.GachaPoolTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

//
object GachaCache {

  lateinit var star1List: MutableList<GachaCharacter>
  lateinit var star2List: MutableList<GachaCharacter>
  lateinit var star3List: MutableList<GachaCharacter>
  lateinit var star2PickupList: MutableList<GachaCharacter>
  lateinit var star3PickupList: MutableList<GachaCharacter>

  fun init() {
    updateData()
    Arona.info("arona gacha module init success.")
  }

  fun updateData() {
    val all = GachaCharacter.find { GachaCharacterTable.limit eq false }
    val limit = GachaCharacterTable.join(GachaPoolCharacterTable, JoinType.INNER,
      additionalConstraint = { GachaCharacterTable.id eq GachaPoolCharacterTable.characterId })
      .join(GachaPoolTable, JoinType.INNER,
        additionalConstraint = { GachaPoolTable.id eq GachaPoolCharacterTable.poolId })
      .selectAll()
      .map {
        GachaCharacter.wrapRow(it)
      }
    star1List = all.filter { it.star == 1 }.toMutableList()
    star2List = all.filter { it.star == 2 }.toMutableList()
    star3List = all.filter { it.star == 3 }.toMutableList()
    star2PickupList = limit.filter { it.star == 2 }.toMutableList()
    star3PickupList = limit.filter { it.star == 3 }.toMutableList()
    println(1)
  }

}