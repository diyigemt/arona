package net.diyigemt.arona.util

import me.towdium.pinin.PinIn
import me.towdium.pinin.utils.PinyinFormat
import net.diyigemt.arona.Arona
import net.diyigemt.arona.command.CallMeCommand
import net.diyigemt.arona.command.TarotCommand
import net.diyigemt.arona.command.TrainerCommand
import net.diyigemt.arona.config.AronaConfig
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.image.ImageTable
import net.diyigemt.arona.db.image.ImageTableModel
import net.diyigemt.arona.db.name.TeacherName
import net.diyigemt.arona.db.name.TeacherNameTable
import net.diyigemt.arona.entity.FuzzyImageResult
import net.diyigemt.arona.entity.ImageRequestResult
import net.diyigemt.arona.interfaces.Initialize
import net.diyigemt.arona.util.NetworkUtil.BACKEND_IMAGE_FOLDER
import net.diyigemt.arona.util.NetworkUtil.downloadImageFile
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.UserOrBot
import net.mamoe.mirai.contact.nameCardOrNick
import org.jetbrains.exposed.sql.and
import java.io.File
import java.security.MessageDigest

object GeneralUtils : Initialize {

  private lateinit var PinyinObject: PinIn
  private val Punctuation0: Regex = Regex("[\\u3002\\uff1f\\uff01\\uff0c\\u3001\\uff1b\\uff1a\\u201c\\u201d\\u2018\\u2019\\uff08\\uff09\\u300a\\u300b\\u3008\\u3009\\u3010\\u3011\\u300e\\u300f\\u300c\\u300d\\ufe43\\ufe44\\u3014\\u3015\\u2026\\u2014\\uff5e\\ufe4f\\uffe5]")
  private val Punctuation1: Regex = Regex("[.,/#!\$%^&*;:{}=\\-_+`~()\\[\\]]")
  private val Punctuation2: Regex = Regex("\\s{2,}")
  const val ConfigFolder: String = "/config"

  fun checkService(group: Contact?): Boolean = when (group) {
    is Group -> AronaConfig.groups.contains(group.id)
    else -> false
  }

  fun clearExtraQute(s: String): String {
    if (s.replace("\"", "").length + 2 == s.length) {
      return s.replaceFirst("\"", "").substring(0, s.length - 2)
    }
    return s
  }

  fun queryTeacherNameFromDB(contact: Contact, user: UserOrBot): String = queryTeacherNameFromDB(contact.id, user)

  fun queryTeacherNameFromDB(contactId: Long, user: UserOrBot): String {
    if (!CallMeCommand.enable) return user.nameCardOrNick
    val name = query {
      TeacherName.find { (TeacherNameTable.group eq contactId) and (TeacherNameTable.id eq user.id) }.firstOrNull()
    }?.name ?: user.nameCardOrNick
    return if (AronaConfig.endWithSensei.isNotBlank() && !name.endsWith(AronaConfig.endWithSensei)) "${name}${AronaConfig.endWithSensei}" else name
  }

  fun randomInt(bound: Int): Int = (System.currentTimeMillis() % bound).toInt()

  fun randomBoolean(): Boolean = System.currentTimeMillis().toString().let {
    it.substring(it.length - 1).toInt() % 2 == 0
  }

  /**
   * 向后端请求图片 更新本地图片数据库并下载
   * 当且仅当远端或本地具有精确匹配结果时才返回图片文件, 否则返回上级进行模糊搜索
   * @param name 精确搜索目标
   * @return 当后端正常响应时, 判断是否为模糊搜索结果, 若为模糊搜索, 返回上级继续处理
   * 若为精确搜索结果, 判断本地是否已有图片, 若有则根据hash判断是否更新图片;
   * 否则下载图片并进行相应操作后返回本地图片文件;
   * 当后端未正常响应, 判断本地精确匹配结果, 若有则返回图片文件, 否则返回上级继续模糊搜索本地图片
   */
  fun loadImageOrUpdate(name: String): ImageRequestResult {
    val localDB = query {
      ImageTableModel.find { ImageTable.name eq name }.firstOrNull()
    }
    val result = NetworkUtil.requestImage(name)
      .onFailure {
      // 服务器寄了 或者精确匹配与模糊查询结果均为空 尝试从本地拿
      // 如果本地数据库有精确匹配结果, 直接发送
      if (localDB != null) {
        val localFile = localImageFile(localDB.path)
        return if (localFile.exists()) ImageRequestResult(file = localFile) else ImageRequestResult()
      }
      it.printStackTrace()
      // 否则对数据库内容和自定义配置文件进行模糊搜索(指令上级)
      return ImageRequestResult()
    }.getOrNull() ?: return ImageRequestResult()
    // 服务器没寄, 判断结果是图片文件还剩模糊查询
    val imageResultList = result.data
    val imageResult = imageResultList[0]
    // 模糊查询结果
    if (imageResult.type == FuzzyImageResult) {
      return ImageRequestResult(list = imageResultList)
    }
    // 精确结果
    val localFile = localImageFile(imageResult.path)
    // 没有本地图片, 向后端下载并存入数据库中
    return if (localDB == null) {
      kotlin.runCatching {
        imageRequest(imageResult.path, localFile)
      }.onFailure {
        Arona.sendMessageToAdmin("在下载图片${imageResult.name}时失败,请查看控制台报错信息")
        it.printStackTrace()
        throw it
      }
      // 将本地图片信息写入数据库
      query {
        ImageTableModel.new {
          this.name = name
          this.path = imageResult.path
          this.hash = imageResult.hash
          this.type = imageResult.type
        }
      }
      ImageRequestResult(file = localFile)
    } else {
      // 有本地图片 如果本地hash值与服务器不一致或者本地文件不存在则获取图片
      return if (localDB.hash != imageResult.hash || !localFile.exists()) {
        // 删除本地图片并重新获取
        if (localDB.path != imageResult.path) {
          localImageFile(localDB.path).delete()
        }
        // 否则直接写入旧文件
        kotlin.runCatching {
          imageRequest(imageResult.path, localFile)
        }.onFailure {
          Arona.sendMessageToAdmin("在下载图片${imageResult.name}时失败,请查看控制台报错信息")
          it.printStackTrace()
          return ImageRequestResult()
        }
        // 更新hash
        query {
          localDB.hash = imageResult.hash
          localDB.path = imageResult.path
          localDB.type = imageResult.type
        }
        ImageRequestResult(file = localFile)
      } else {
        ImageRequestResult(file = localFile)
      }
    }
  }

  private fun replacePunctuation(str: String): String = str.replace(Punctuation0, "")
    .replace(Punctuation1, "")
    .replace(Punctuation2, "")

  fun md5(str: String): String = MessageDigest.getInstance("MD5").digest(str.toByteArray(Charsets.UTF_8)).toHex()
  fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }

  fun imageRequest(path: String, localFile: File): Result<File> = downloadImageFile(path, localFile)

  private fun imageFileFolder(subFolder: String = "") = Arona.dataFolderPath(BACKEND_IMAGE_FOLDER) + subFolder

  fun localImageFile(path: String) =
    File(imageFileFolder(path.let { return@let if (path.startsWith("/")) path else "/$it" }))

  override val priority: Int = 1
  override fun init() {
    // 初始化本地图片文件夹
    File(imageFileFolder(TrainerCommand.ChapterMapFolder)).also { it.mkdirs() }
    File(imageFileFolder(TrainerCommand.StudentRankFolder)).also { it.mkdirs() }
    File(imageFileFolder(TrainerCommand.OtherFolder)).also { it.mkdirs() }
    File(imageFileFolder(TarotCommand.TarotImageFolder)).also { it.mkdirs() }
    File(Arona.dataFolderPath(ConfigFolder)).also { it.mkdirs() }
    PinyinObject = PinIn().config().format(PinyinFormat.RAW).fSh2S(true).commit()
  }
}