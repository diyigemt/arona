package net.diyigemt.arona.util
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
object ImageUtil {

  private const val DEFAULT_ITEM_SIZE: Int = 72
  fun createCalendarImage(eventLength: Int, titleMaxLength: Int): BufferedImage {
    val width = DEFAULT_ITEM_SIZE * (titleMaxLength + 10) * 0.7
    val height = (eventLength + 2) * DEFAULT_ITEM_SIZE
    val img = BufferedImage(width.toInt(), height, BufferedImage.TYPE_4BYTE_ABGR)
    val g = img.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
      RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    return img
  }

  fun init(img: BufferedImage, color: Color) {
    val g = img.graphics
    g.color = color
    g.fillRect(0, 0, img.width, img.height)
  }

  fun drawRoundRect(img: BufferedImage, x: Int, y: Int, w: Int, h: Int, r: Int, color: Color) {
    val g = img.graphics
    g.color = color
    g.fillRoundRect(x, y, w, h, r, r)
  }

  fun drawText(img: BufferedImage, str: String, x: Int, y: Int, align: TextAlign, color: Color) {
    val g = img.graphics
//    g.font = g.font.deriveFont(Font.BOLD).deriveFont(DEFAULT_ITEM_SIZE.toFloat())
    g.font = Font("微软雅黑", Font.BOLD, DEFAULT_ITEM_SIZE)
    g.color = color
    val width = g.fontMetrics.stringWidth(str)
    when (align) {
      TextAlign.LEFT -> g.drawString(str, x, y)
      TextAlign.RIGHT -> g.drawString(str, img.width - width, y)
      TextAlign.CENTER -> g.drawString(str, (img.width - x - width) / 2 + x, y)
    }
  }

  fun drawTextByLine(img: BufferedImage, str: String, line: Int, align: TextAlign, color: Color = Color.BLACK) {
    drawText(img, str, 0, line + DEFAULT_ITEM_SIZE, align, color)
  }

  enum class TextAlign {
    LEFT, RIGHT, CENTER
  }
}

