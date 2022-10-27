package net.diyigemt.arona.entity.dto.data

import net.diyigemt.arona.annotations.DTOService
import net.diyigemt.arona.db.data.schaledb.Raid as RaidDSL
import net.diyigemt.arona.entity.dto.BaseDTO
import org.jetbrains.exposed.sql.ResultRow

/**
 *@Author hjn
 *@Create 2022/10/25
 */
@DTOService
data class Raid(
  val id : Int = 0,
  val IsReleased : Boolean = false,
  val NameCn : String = "",
  val CurrentGLB : String = "",
  val CurrentJPN : String = ""
) : BaseDTO<Raid>{
  override fun toModel(results: List<ResultRow>): List<Raid> {
    val res : MutableList<Raid> = mutableListOf()
    results.forEach {
      res.add(Raid(it[RaidDSL.Id], it[RaidDSL.IsReleased], it[RaidDSL.NameCn], it[RaidDSL.CurrentGLB], it[RaidDSL.CurrentJPN]))
    }

    return res
  }

}
