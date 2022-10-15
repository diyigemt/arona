package net.diyigemt.arona.util.scbaleDB.factories

import net.diyigemt.arona.entity.schaleDB.LocalizationDAO
import net.diyigemt.arona.entity.schaleDB.Raid
import net.diyigemt.arona.entity.schaleDB.RaidDAO
import net.diyigemt.arona.entity.schaleDB.StudentDAO
import net.diyigemt.arona.util.scbaleDB.SchaleDBUtil

/**
 *@Author hjn
 *@Create 2022/8/20
 */
object CalendarFactory {
  fun getEventLocalizationName(eventID : Int) : String{
    val localizationData: LocalizationDAO = SchaleDBUtil.localizationItem
    val res = localizationData.EventName[eventID.toString()]

    return res ?: ""
  }

  fun getCharacterLocalizationName(characterIDList : List<Int>) : String{
    val studentData: StudentDAO = SchaleDBUtil.studentItem
    var res = ""
    for (item in characterIDList){
      res += studentData.getStudentNameById(item) + " "
    }

    return res
  }

  fun getRaidLocalizationName(raidID : Int) : String {
    val raidData: RaidDAO = SchaleDBUtil.raidItem
    val res = raidData.getRaidNameById(raidID)

    return res ?: ""
  }

  fun getRaidById(raidID: Int) : Raid?{
    for (item in SchaleDBUtil.raidItem.Raid){
      if (raidID == item.Id) return item
    }

    return null
  }

  fun getRaidStatus(raidID: Int) : Boolean = SchaleDBUtil.raidItem.isRaidReleased(raidID)
}