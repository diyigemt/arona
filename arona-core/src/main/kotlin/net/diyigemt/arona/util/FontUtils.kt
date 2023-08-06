package net.diyigemt.arona.util

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.*
import java.util.*
import kotlin.collections.*
import kotlin.jvm.*

/**
 * @author cssxsh
 * @see [https://github.com/cssxsh/mirai-skia-plugin/blob/main/src/main/kotlin/xyz/cssxsh/skia/FontUtils.kt]
 */
/**
 * 获取字体工具
 * @see Typeface
 * @see FontMgr
 * @see TypefaceFontProvider
 * @see [https://docs.microsoft.com/en-us/typography/fonts/windows_10_font_list]
 * @see [https://github.com/cssxsh/mirai-skia-plugin/blob/main/src/main/kotlin/xyz/cssxsh/skia/FontUtils.kt]
 */
public object FontUtils {

  @PublishedApi
  internal val instances: Sequence<FontMgr> = sequence {
    yield(provider)
    yield(FontMgr.default)
    yieldAll(ServiceLoader.load(FontMgr::class.java))
    yieldAll(ServiceLoader.load(TypefaceFontProvider::class.java))
  }

  public val provider: TypefaceFontProvider = TypefaceFontProvider()

  /**
   * 字体列表
   */
  public fun families(): Set<String> {
    val names: MutableSet<String> = HashSet()
    for (manager in instances) {
      repeat(manager.familiesCount) { index -> names.add(manager.getFamilyName(index)) }
    }

    return names
  }

  /**
   * 加载字体
   * @see provider
   */
  public fun loadTypeface(path: String, index: Int = 0) {
    provider.registerTypeface(Typeface.makeFromFile(path, index))
  }

  /**
   * 加载字体
   * @see provider
   */
  public fun loadTypeface(data: Data, index: Int = 0) {
    provider.registerTypeface(Typeface.makeFromData(data, index))
  }

  /**
   * 加载字体
   * @see provider
   */
  public fun loadTypeface(bytes: ByteArray, index: Int = 0) {
    Data.makeFromBytes(bytes).use { data -> loadTypeface(data, index) }
  }

  /**
   * 获取指定的 [Typeface]
   */
  public fun matchFamilyStyle(familyName: String, style: FontStyle): Typeface? {
    for (provider in instances) {
      return provider.matchFamily(familyName).matchStyle(style) ?: continue
    }
    return null
  }

  /**
   * 获取指定的 [Typeface]
   */
  public fun matchFamiliesStyle(families: Array<String?>, style: FontStyle): Typeface? {
    for (provider in instances) {
      for (familyName in families) {
        return provider.matchFamily(familyName).matchStyle(style) ?: continue
      }
    }
    return null
  }

  /**
   * 获取指定的 [FontStyleSet] (count != 0)
   */
  public fun matchFamily(familyName: String): FontStyleSet? {
    for (provider in instances) {
      val styles = provider.matchFamily(familyName)
      if (styles.count() != 0) {
        return styles
      }
    }
    return null
  }

  /**
   * 宋体
   */
  public fun matchSimSun(style: FontStyle): Typeface? = matchFamilyStyle("SimSun", style)

  /**
   * 新宋体
   */
  public fun matchNSimSun(style: FontStyle): Typeface? = matchFamilyStyle("NSimSun", style)

  /**
   * 黑体
   */
  public fun matchSimHei(style: FontStyle): Typeface? = matchFamilyStyle("SimHei", style)

  /**
   * 仿宋
   */
  public fun matchFangSong(style: FontStyle): Typeface? = matchFamilyStyle("FangSong", style)

  /**
   * 楷体
   */
  public fun matchKaiTi(style: FontStyle): Typeface? = matchFamilyStyle("KaiTi", style)

  /**
   * Arial
   */
  public fun matchArial(style: FontStyle): Typeface? = matchFamilyStyle("Arial", style)

  /**
   * Calibri
   */
  public fun matchCalibri(style: FontStyle): Typeface? = matchFamilyStyle("Calibri", style)

  /**
   * Consolas
   */
  public fun matchConsolas(style: FontStyle): Typeface? = matchFamilyStyle("Consolas", style)

  /**
   * Times New Roman
   */
  public fun matchTimesNewRoman(style: FontStyle): Typeface? = matchFamilyStyle("Times New Roman", style)

  /**
   * Helvetica
   */
  public fun matchHelvetica(style: FontStyle): Typeface? = matchFamilyStyle("Helvetica", style)

  /**
   * Liberation Sans
   */
  public fun matchLiberationSans(style: FontStyle): Typeface? = matchFamilyStyle("Liberation Sans", style)

  /**
   * Liberation Serif
   */
  public fun matchLiberationSerif(style: FontStyle): Typeface? = matchFamilyStyle("Liberation Serif", style)

  /**
   * Noto Color Emoji
   */
  public fun matchNotoColorEmoji(style: FontStyle): Typeface? = matchFamilyStyle("Noto Color Emoji", style)
}
