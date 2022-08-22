package net.diyigemt.arona.util
import net.diyigemt.arona.entity.HelperTableColWidth
import net.diyigemt.arona.entity.StageMapHelperTable
import net.diyigemt.arona.util.ActivityUtil.DEFAULT_CALENDAR_FONT_SIZE
import net.diyigemt.arona.util.ActivityUtil.DEFAULT_CALENDAR_LINE_MARGIN
import net.diyigemt.arona.util.ActivityUtil.DEFAULT_IMAGE_SCALE
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

object ImageUtil {

  private const val DEFAULT_PADDING: Int = 10
  fun createCalendarImage(eventLength: Int, contentMaxLength: Int, titleLength: Int = 20, fontSize: Float = DEFAULT_CALENDAR_FONT_SIZE.toFloat()): Pair<BufferedImage, Graphics2D> {
    val width = fontSize * (max(contentMaxLength, titleLength) + 20) * 0.7
    val height = (eventLength + 3) * (fontSize + DEFAULT_CALENDAR_LINE_MARGIN) + 2 * DEFAULT_CALENDAR_LINE_MARGIN
    val img = BufferedImage(width.toInt(), height.toInt(), BufferedImage.TYPE_4BYTE_ABGR)
    val g = img.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
      RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    g.font = g.font.deriveFont(Font.PLAIN).deriveFont(fontSize)
    return img to g
  }

  fun createStageMapHelper(imgUrl: String, table: StageMapHelperTable): BufferedImage? {
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
        return@outer if (cell.content.contains("\n")) 2 else 1
      }.sum() > cells.size) 2 else 1
    }.sum() + 1
    val textWidth = table.col * HelperTableColWidth
    val bigBaseMapHeight = (baseMap.height / DEFAULT_IMAGE_SCALE).toInt() + 2 * DEFAULT_CALENDAR_FONT_SIZE
    val width = max((baseMap.width / DEFAULT_IMAGE_SCALE).toInt(), textWidth)
    val height = bigBaseMapHeight + trueRow * (DEFAULT_CALENDAR_FONT_SIZE + DEFAULT_CALENDAR_LINE_MARGIN)
    val img = BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
    val g = img.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
      RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    g.font = g.font.deriveFont(Font.PLAIN).deriveFont(DEFAULT_CALENDAR_FONT_SIZE.toFloat())
    val group = img to g
    val header = table.header
    val content = table.content
    val startX = (img.width - textWidth) / 2
    var currentRow = 0
    fun calcOffsetX(s: String, col: Int, span: Int = 1): Int = startX + col * HelperTableColWidth + (HelperTableColWidth * span - g.fontMetrics.stringWidth(s)) / 2 - DEFAULT_PADDING
    fun calcOffsetY(row: Int = currentRow, offset: Int = 0): Int = bigBaseMapHeight + DEFAULT_CALENDAR_FONT_SIZE * (row + offset) + DEFAULT_CALENDAR_LINE_MARGIN * (row + offset - 1)
    // 画表头
    (0 until table.col).forEach {
      drawText(group, header[it], calcOffsetX(header[it], it), calcOffsetY())
    }
    currentRow++
    // 画内容
    var col = 0
    content.forEach {
      var doubleLineFlag = false
      while (col < table.col) {
        val target = it[col]
        val contents = target.content
        if (contents.contains("\n") && contents.length > 12) {
          val a = contents.substringBefore("\n")
          val b = contents.substringAfter("\n")
          drawText(group, a, calcOffsetX(a, col, span = target.colspan), calcOffsetY())
          currentRow++
          drawText(group, b, calcOffsetX(b, col, span = target.colspan), calcOffsetY())
          currentRow--
          doubleLineFlag = true
        } else {
          drawText(group, target.content, calcOffsetX(target.content, col, span = target.colspan), calcOffsetY())
        }
        col += target.colspan
      }
      if (doubleLineFlag) {
        currentRow++
      }
      currentRow++
      col = 0
    }
    val send = img.scale(DEFAULT_IMAGE_SCALE)
    send.createGraphics().drawImage(baseMap, null, (send.width - baseMap.width) / 2, 0)
    ImageIO.write(send, "png", File("test.png"))
    return null
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

  fun drawText(
    group: Pair<BufferedImage, Graphics2D>,
    str: String,
    x: Int,
    y: Int,
    align: TextAlign = TextAlign.LEFT,
    color: Color = Color.BLACK,
    fontWeight: Int = Font.PLAIN
  ) {
    val g = group.second
    val img = group.first
    g.color = color
    g.font = g.font.deriveFont(fontWeight)
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
    fontWeight: Int = Font.PLAIN
  ) {
    drawText(group, str, 0, y, align, color, fontWeight)
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
}

