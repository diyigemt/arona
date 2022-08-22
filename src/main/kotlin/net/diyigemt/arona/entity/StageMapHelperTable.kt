package net.diyigemt.arona.entity

import net.diyigemt.arona.util.ActivityUtil.DEFAULT_CALENDAR_FONT_SIZE

const val HelperTableColFontSizeWidth = 15
const val HelperTableColWidth = HelperTableColFontSizeWidth * DEFAULT_CALENDAR_FONT_SIZE

data class StageMapHelperTable(
  val row: Int,
  val col: Int,
  val header: List<String>,
  val content: List<List<TableCell>>
) {
  data class TableCell(
    val content: String,
    val colspan: Int = 1
  )
}