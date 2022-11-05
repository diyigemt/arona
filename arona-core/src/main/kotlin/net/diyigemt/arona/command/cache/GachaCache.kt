package net.diyigemt.arona.command.cache

import net.diyigemt.arona.Arona
import net.diyigemt.arona.config.AronaGachaConfig
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.gacha.*
import org.jetbrains.exposed.sql.select


object GachaCache {

  lateinit var star1List: MutableList<GachaCharacters>
  lateinit var star2List: MutableList<GachaCharacters>
  lateinit var star3List: MutableList<GachaCharacters>
  lateinit var star2PickupList: MutableList<GachaCharacters>
  lateinit var star3PickupList: MutableList<GachaCharacters>

  fun init() {
    updateData()
    Arona.info("arona gacha module init success.")
  }

  fun updatePool(pool: Int): GachaPools? {
    val targetPool = query {
      GachaPools.findById(pool)
    } ?: return null
    if (!updateLimitData(pool)) return null
    AronaGachaConfig.activePool = pool
    return targetPool
  }

  private fun updateData() {
    val all = query {
      GachaCharacters.find { GachaCharactersTable.limit eq false }
    }!!
    star1List = all.filter { it.star == 1 }.toMutableList()
    star2List = all.filter { it.star == 2 }.toMutableList()
    star3List = all.filter { it.star == 3 }.toMutableList()
    updateLimitData(AronaGachaConfig.activePool)
  }

  private fun updateLimitData(pool: Int): Boolean {
    val limit = query {
      GachaCharactersTable
        .innerJoin(GachaPoolCharactersTable)
        .innerJoin(GachaPoolsTable)
        .select { GachaPoolsTable.id eq pool }
        .map {
          GachaCharacters.wrapRow(it)
        }
    }?: return false
    star2PickupList = limit.filter { it.star == 2 }.toMutableList()
    star3PickupList = limit.filter { it.star == 3 }.toMutableList()
    return true
  }
}