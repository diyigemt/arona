package net.diyigemt.arona.util
import net.diyigemt.arona.Arona
import net.diyigemt.arona.entity.*
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.util.ActivityUtil.DEFAULT_CALENDAR_FONT_SIZE
import net.diyigemt.arona.util.ActivityUtil.DEFAULT_CALENDAR_LINE_MARGIN
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max

object ImageUtil : InitializedFunction() {

  private const val DEFAULT_PADDING: Int = 10
  private const val FONT_NAME = "SourceHanSansCN-Normal.otf"
  private const val FontFolder: String = "font"
  private var font: Font? = null

  fun createCalendarImage(eventLength: Int, contentMaxLength: Int, titleLength: Int = 20, fontSize: Float = DEFAULT_CALENDAR_FONT_SIZE.toFloat()): Pair<BufferedImage, Graphics2D> {
    val width = fontSize * (max(contentMaxLength, titleLength) + 20) * 0.8
    val height = (eventLength + 4) * (fontSize + DEFAULT_CALENDAR_LINE_MARGIN) + 2 * DEFAULT_CALENDAR_LINE_MARGIN
    val img = BufferedImage(width.toInt(), height.toInt(), BufferedImage.TYPE_4BYTE_ABGR)
    val g = img.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
      RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    g.font = g.font.deriveFont(Font.PLAIN).deriveFont(fontSize)
    font = font?.deriveFont(Font.PLAIN)?.deriveFont(fontSize)
    return img to g
  }

  fun createStageMapHelper(banner: String, imgUrl: String, table: StageMapHelperTable): BufferedImage? {
    val builder = OkHttpClient.Builder()
    val client = builder.build()
    val request = Request.Builder()
      .url(imgUrl)
      .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
      .get()
      .build()
    val response = client.newCall(request).execute()
    if (!response.isSuccessful) return null
    val stream = response.body?.byteStream() ?: return null
    val baseMap = ImageIO.read(stream)
    stream.close()
    // 计算有()备注内容的换行
    val trueRow = table.content.map { cells ->
      return@map if (cells.map outer@ { cell ->
        return@outer if (cell.content.length > DEFAULT_TABLE_SPLIT_LINE * cell.colspan) 2 else 1
      }.sum() > cells.size) 2 else 1
    }.sum() + 3 // 表头一行 + 标题一行 + 最后出处声明
    val textWidth = table.col * HelperTableColWidth
    val tableLineX = textWidth - DEFAULT_TABLE_BORDER_PADDING_LEFT
    val bigBaseMapHeight = (baseMap.height / DEFAULT_TABLE_IMAGE_SCALE).toInt() + 2 * DEFAULT_TABLE_FONT_SIZE
    val width = max((baseMap.width / DEFAULT_TABLE_IMAGE_SCALE).toInt(), textWidth)
    val height = bigBaseMapHeight + trueRow * (DEFAULT_TABLE_FONT_SIZE + DEFAULT_TABLE_LINE_MARGIN)
    val img = BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
    val g = img.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
      RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    g.font = g.font.deriveFont(Font.PLAIN).deriveFont(DEFAULT_TABLE_FONT_SIZE.toFloat())
    val group = img to g
    val header = table.header
    val content = table.content
    val startX = (img.width - textWidth) / 2
    var currentRow = 0
    fun calcOffsetX(s: String, col: Int, span: Int = 1): Int = startX + col * HelperTableColWidth + (HelperTableColWidth * span - g.fontMetrics.stringWidth(s)) / 2 - DEFAULT_PADDING
    fun calcOffsetY(row: Int = currentRow, offset: Int = 0): Int = bigBaseMapHeight + DEFAULT_TABLE_FONT_SIZE * (row + offset) + DEFAULT_TABLE_LINE_MARGIN * (row + offset - 1)
    val tableBorderOffsetLeft = calcOffsetX(DEFAULT_TABLE_WHITE_SPACE, 0) + DEFAULT_TABLE_BORDER_PADDING_TOP
    drawTextAlign(group, banner, calcOffsetX(banner, 0, header.size), calcOffsetY() + (DEFAULT_TABLE_FONT_SIZE * 0.5).toInt(), fontSize = (DEFAULT_TABLE_FONT_SIZE * 1.5).toFloat())
    currentRow = 2
    // 画表头
    drawLine(group, tableBorderOffsetLeft, calcOffsetY(offset = -1), tableLineX - DEFAULT_TABLE_BORDER_PADDING_LEFT, DEFAULT_TABLE_BORDER_HEIGHT)
    (0 until table.col).forEach {
      drawTextAlign(group, header[it], calcOffsetX(header[it], it), calcOffsetY())
    }
    currentRow++
    // 画内容
    var col = 0
    content.forEach {
      drawLine(group, tableBorderOffsetLeft, calcOffsetY(offset = -1) + DEFAULT_TABLE_BORDER_PADDING_TOP, tableLineX - DEFAULT_TABLE_BORDER_PADDING_LEFT, ((DEFAULT_TABLE_BORDER_HEIGHT) * 0.5).toInt())
      var doubleLineFlag = false

      // 有换行的情况下垂直居中这一列
      val needWarp =
        it.any { cell -> cell.content.length > DEFAULT_TABLE_SPLIT_LINE * cell.colspan }
      val warpOffsetY = if (needWarp) DOUBLE_LINE_OFFSET else 0
      it.forEach { tableCell ->
        val contents = tableCell.content
        // 多行过长进行换行
        if (contents.length > DEFAULT_TABLE_SPLIT_LINE * tableCell.colspan) {
          val delimiter = contents.substring(DEFAULT_TABLE_SPLIT_LINE - 1, DEFAULT_TABLE_SPLIT_LINE)
          val a = contents.substringBefore(delimiter) + delimiter
          val b = contents.substringAfter(delimiter)
          drawTextAlign(group, a, calcOffsetX(a, col, span = tableCell.colspan), calcOffsetY())
          currentRow++
          drawTextAlign(group, b, calcOffsetX(b, col, span = tableCell.colspan), calcOffsetY())
          currentRow--
          doubleLineFlag = true
        } else {
          drawTextAlign(
            group,
            tableCell.content,
            calcOffsetX(tableCell.content, col, span = tableCell.colspan),
            calcOffsetY() + warpOffsetY
          )
        }
        col += tableCell.colspan
      }
      if (doubleLineFlag) {
        currentRow++
      }
      currentRow++
      col = 0
    }
    // 画表尾
    drawLine(group, tableBorderOffsetLeft, calcOffsetY(offset = -1) + DEFAULT_TABLE_LINE_MARGIN, tableLineX - DEFAULT_TABLE_BORDER_PADDING_LEFT, DEFAULT_TABLE_BORDER_HEIGHT)
    // 画数据来源
    drawTextAlign(group, "数据来源:https://bluearchive.wikiru.jp/", 0, calcOffsetY() - DEFAULT_TABLE_BORDER_HEIGHT, align = TextAlign.RIGHT, fontSize = DEFAULT_TABLE_FONT_SIZE.toFloat() / 2)
    val send = img.scale(DEFAULT_TABLE_IMAGE_SCALE)
    send.createGraphics().drawImage(baseMap, null, (send.width - baseMap.width) / 2, 0)
    return send
  }

  fun init(group: Pair<BufferedImage, Graphics2D>, color: Color) {
    val g = group.second
    val img = group.first
    g.color = color
    g.fillRect(0, 0, img.width, img.height)
  }

  fun drawRoundRect(group: Pair<BufferedImage, Graphics2D>, x: Int, y: Int, w: Int, h: Int, r: Int, color: Color) {
    val g = group.second
    g.color = color
    g.fillRoundRect(x, y, w, h, r, r)
  }

  fun drawLine(
    group: Pair<BufferedImage, Graphics2D>,
    x: Int,
    y: Int,
    w: Int,
    h: Int,
    type: LineType = LineType.HORIZON,
    color: Color = Color.BLACK
  ) {
    val g = group.second
    g.color = color
    when(type) {
      LineType.HORIZON -> g.fillRect(x, y, w, h)
      LineType.VERTICAL -> g.fillRect(x, y, h, w)
    }
  }

  fun drawTextAlign(
    group: Pair<BufferedImage, Graphics2D>,
    str: String,
    x: Int,
    y: Int,
    align: TextAlign = TextAlign.LEFT,
    color: Color = Color.BLACK,
    fontWeight: Int = Font.PLAIN,
    fontSize: Float? = null
  ) {
    val g = group.second
    val img = group.first
    g.font = font ?: g.font
    g.color = color
    g.font = g.font.deriveFont(fontWeight)
    if (fontSize != null) {
      g.font = g.font.deriveFont(fontSize)
    }
    val width = g.fontMetrics.stringWidth(str)
    when (align) {
      TextAlign.LEFT -> g.drawString(str, x + DEFAULT_PADDING, y)
      TextAlign.RIGHT -> g.drawString(str, img.width - width - DEFAULT_PADDING, y)
      TextAlign.CENTER -> g.drawString(str, (img.width - x - width) / 2 + x, y)
    }
  }

  fun drawText(
    group: Pair<BufferedImage, Graphics2D>,
    str: String,
    y: Int,
    align: TextAlign = TextAlign.LEFT,
    color: Color = Color.BLACK,
    fontWeight: Int = Font.PLAIN,
  ) {
    drawTextAlign(group, str, 0, y, align, color, fontWeight)
  }

  fun BufferedImage.scale(x: Float, y: Float, color: Color = Color.WHITE): BufferedImage {
    val width = (this.width * x).toInt()
    val height = (this.height * y).toInt()
    val scale = this.getScaledInstance(width, height, Image.SCALE_SMOOTH)
    val after = BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
    after.graphics.drawImage(scale, 0, 0, color, null)
    return after
  }

  fun BufferedImage.scale(scale: Float, color: Color = Color.WHITE) = this.scale(scale, scale, color)

  enum class TextAlign {
    LEFT, RIGHT, CENTER
  }

  enum class LineType {
    HORIZON, VERTICAL
  }

  override fun init() {
    // 下载字体
    Arona.runSuspend {
      kotlin.runCatching {
        File(Arona.dataFolderPath(FontFolder)).also { it.mkdirs() }
        val path = "/${FontFolder}/$FONT_NAME"
        val fontFile = Arona.dataFolderFile(path)
        if (!fontFile.exists()) {
          NetworkUtil.downloadFileFile(path, fontFile)
        }
        val f = Font.createFont(Font.TRUETYPE_FONT, fontFile)
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f)
        font = f
        Arona.info("中文字体下载成功")
      }.onFailure {
        Arona.warning("字体注册失败, 使用默认字体, 可能会导致中文乱码")
      }
    }
  }
}

