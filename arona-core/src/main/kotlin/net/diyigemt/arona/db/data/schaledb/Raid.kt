package net.diyigemt.arona.db.data.schaledb

import net.diyigemt.arona.annotations.DTOService
import net.diyigemt.arona.db.BaseDTO
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

/**
 *@Author hjn
 *@Create 2022/8/28
 */
@DTOService
object Raid : Table("Raid") {
  val Id : Column<Int> = integer("Id")
  val IsReleased : Column<Boolean> = bool("IsReleased")
  val NameCn : Column<String> = varchar("NameCn", 20)
  val CurrentJPN : Column<String> = varchar("CurrentJPN", 20)
  val CurrentGLB : Column<String> = varchar("CurrentGLB", 20)

  data class RaidDTO(
    val Id : Int = 0,
    val IsReleased : Boolean = false,
    val NameCn : String = "",
    val CurrentGLB : String = "",
    val CurrentJPN : String = ""
  ) : BaseDTO<RaidDTO> {
    override fun toModel(results: List<ResultRow>): List<RaidDTO> {
      val res : MutableList<RaidDTO> = mutableListOf()
      results.forEach {
        res.add(RaidDTO(it[Raid.Id], it[Raid.IsReleased], it[Raid.NameCn], it[Raid.CurrentGLB], it[Raid.CurrentJPN]))
      }

      return res
    }
  }
}