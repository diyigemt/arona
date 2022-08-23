package net.diyigemt.arona.entity

const val DEFAULT_TABLE_PADDING = 20
const val DEFAULT_TABLE_LINE_MARGIN = 50
const val DEFAULT_TABLE_FONT_SIZE = 144
const val DEFAULT_TABLE_BORDER_HEIGHT = DEFAULT_TABLE_LINE_MARGIN - DEFAULT_TABLE_PADDING
const val DEFAULT_TABLE_BORDER_PADDING_LEFT = DEFAULT_TABLE_FONT_SIZE * 3
const val DEFAULT_TABLE_BORDER_PADDING_TOP = 40
const val DEFAULT_TABLE_IMAGE_SCALE = 0.2F
const val HelperTableColFontLength = 15
val DEFAULT_TABLE_WHITE_SPACE = (1..12).joinToString("") { "A" }
const val DEFAULT_TABLE_SPLIT_LINE = HelperTableColFontLength - 3
const val HelperTableColWidth = HelperTableColFontLength * DEFAULT_TABLE_FONT_SIZE

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