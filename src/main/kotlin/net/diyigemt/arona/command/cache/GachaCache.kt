package net.diyigemt.arona.command.cache
//
import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.db.gacha.GachaCharacter
import net.diyigemt.arona.db.gacha.GachaCharacterTable
import net.diyigemt.arona.db.gacha.GachaPoolCharacterTable
import net.diyigemt.arona.db.gacha.GachaPoolTable
import org.jetbrains.exposed.sql.select

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
    star1List = all.filter { it.star == 1 }.toMutableList()
    star2List = all.filter { it.star == 2 }.toMutableList()
    star3List = all.filter { it.star == 3 }.toMutableList()
    updateLimitData(AronaGachaConfig.defaultActivePool)
  }
  fun updateLimitData(pool: Int) {
    val limit = GachaCharacterTable
      .innerJoin(GachaPoolCharacterTable)
      .innerJoin(GachaPoolTable)
      .select { GachaPoolTable.id eq pool }
      .map {
        GachaCharacter.wrapRow(it)
      }
    star2PickupList = limit.filter { it.star == 2 }.toMutableList()
    star3PickupList = limit.filter { it.star == 3 }.toMutableList()
  }

}