package net.diyigemt.arona.command.cache

import net.diyigemt.arona.Arona
import net.diyigemt.arona.db.model.GachaCharacter

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
    val all = GachaCharacter.all()
    star1List = all.filter { it.star == 1 && !it.limit }.toMutableList()
    val star2ListTmp = all.filter { it.star == 2 }.toMutableList()
    val star3ListTmp = all.filter { it.star == 3 }.toMutableList()
    star2List = star2ListTmp.filter { !it.pickup && !it.limit }.toMutableList()
    star3List = star3ListTmp.filter { !it.pickup && !it.limit }.toMutableList()
    star2PickupList = star2ListTmp.filter { it.pickup }.toMutableList()
    star3PickupList = star3ListTmp.filter { it.pickup }.toMutableList()
  }

}