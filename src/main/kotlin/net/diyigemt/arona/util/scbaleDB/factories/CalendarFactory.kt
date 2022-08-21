package net.diyigemt.arona.util.scbaleDB.factories

import net.diyigemt.arona.entity.schaleDB.LocalizationDAO
import net.diyigemt.arona.entity.schaleDB.RaidDAO
import net.diyigemt.arona.entity.schaleDB.StudentDAO
import net.diyigemt.arona.util.scbaleDB.SchaleDBUtil

/**
 *@Author hjn
 *@Create 2022/8/20
 */
object CalendarFactory {
  fun getEventLocalizationName(eventID : Int) : String{
    val localizationData: LocalizationDAO = SchaleDBUtil.getLocalizationData()
    val res = localizationData.EventName.getValueById(eventID)

    return res ?: ""
  }

  fun getCharacterLocalizationName(characterIDList : List<Int>) : String{
    val studentData: StudentDAO = SchaleDBUtil.getStudentData()
    var res = ""
    for (item in characterIDList){
      res += studentData.getStudentNameById(item) + " "
    }

    return res
  }

  fun getRaidLocalizationName(raidID : Int) : String {
    val raidData: RaidDAO = SchaleDBUtil.getRaidData()
    val res = raidData.getRaidNameById(raidID)

    return res ?: ""
  }
}