package net.diyigemt.arona.util
import net.diyigemt.arona.Arona
import net.diyigemt.arona.entity.*
import net.diyigemt.arona.interfaces.InitializedFunction
import net.diyigemt.arona.util.ActivityUtil.DEFAULT_CALENDAR_FONT_SIZE
import net.diyigemt.arona.util.ActivityUtil.DEFAULT_CALENDAR_LINE_MARGIN
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.skia.*
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max

object ImageUtil : InitializedFunction() {

  private const val DEFAULT_PADDING: Int = 10
  private const val FONT_NAME = "SourceHanSansCN-Normal.otf"
  private const val FontFolder: String = "/font"
  private lateinit var font: Font

  fun createCalendarImage(eventLength: Int, contentMaxLength: Int, titleLength: Int = 20, fontSize: Float = DEFAULT_CALENDAR_FONT_SIZE.toFloat()): Pair<Surface, Font> {
    val width = fontSize * (max(contentMaxLength, titleLength) + 20) * 0.8
    val height = (eventLength + 4) * (fontSize + DEFAULT_CALENDAR_LINE_MARGIN) + 2 * DEFAULT_CALENDAR_LINE_MARGIN
    return Surface.makeRasterN32Premul(width.toInt(), height.toInt()) to font.makeWithSize(fontSize)
  }

  fun init(surface: Surface, color: Int = 0xFFFFFFFF.toInt()) {
    surface.canvas.clear(color)
  }

  fun drawRoundRect(surface: Surface, x: Int, y: Int, w: Int, h: Int, r: Int, color: Int) {
    surface.canvas.drawRRect(RRect.makeLTRB(x.toFloat(), y.toFloat(), (x + w).toFloat(), (y + h).toFloat(), r.toFloat()), Paint().also { it.color = color })
  }

  private fun drawTextAlign(
    group: Pair<Surface, Font>,
    str: String,
    x: Int,
    y: Int,
    align: TextAlign = TextAlign.LEFT,
    color: Int = 0xFF000000.toInt(),
  ) {
    val surface = group.first
    val canvas = surface.canvas
    val font = group.second
    val width = font.measureTextWidth(str)
    when (align) {
      TextAlign.LEFT -> canvas.drawString(str, (x + DEFAULT_PADDING).toFloat(), y.toFloat(), font, Paint(color))
      TextAlign.RIGHT -> canvas.drawString(str, (surface.width - width - DEFAULT_PADDING), y.toFloat(), font, Paint(color))
      TextAlign.CENTER -> canvas.drawString(str, ((surface.width - x - width) / 2 + x), y.toFloat(), font, Paint(color))
    }
  }

  fun drawText(
    group: Pair<Surface, Font>,
    str: String,
    y: Int,
    align: TextAlign = TextAlign.LEFT,
    color: Int = 0xFF000000.toInt(),
  ) {
    drawTextAlign(group, str, 0, y, align, color)
  }

  fun Surface.scale(x: Float, y: Float, color: Int = 0xFFFFFFFF.toInt()): Surface {
    val width = (this.width * x).toInt()
    val height = (this.height * y).toInt()
    val after = Surface.makeRasterN32Premul(width, height)
    after.canvas.clear(color)
    after.canvas.scale(x, y).drawImage(this.makeImageSnapshot(), 0f, 0f)
    return after
  }

  private fun Paint(color: Int) = Paint().also { it.color = color }

  enum class TextAlign {
    LEFT, RIGHT, CENTER
  }

  override fun init() {
    // 下载字体
    Arona.runSuspend {
      kotlin.runCatching {
        File(Arona.dataFolderPath(FontFolder)).also { it.mkdirs() }
        val path = "$FontFolder/$FONT_NAME"
        val fontFile = Arona.dataFolderFile(path)
        if (!fontFile.exists()) {
          NetworkUtil.downloadFileFile(path, fontFile)
        }
        val tf = Typeface.makeFromFile(fontFile.path)
        font = Font(tf).also {
          it.edging = FontEdging.SUBPIXEL_ANTI_ALIAS
          it.hinting = FontHinting.FULL
        }
        Arona.info("中文字体初始化成功")
      }.onFailure {
        font = Font(Typeface.makeDefault())
        Arona.warning("字体注册失败, 使用默认字体, 可能会导致中文乱码")
      }
    }
  }
}

