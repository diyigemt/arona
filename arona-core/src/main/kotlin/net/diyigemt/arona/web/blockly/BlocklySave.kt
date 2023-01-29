package net.diyigemt.arona.web.blockly

import kotlinx.serialization.json.Json
import net.diyigemt.arona.Arona
import net.lingala.zip4j.core.ZipFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 *@Author hjn
 *@Create 2023/1/3
 */
class BlocklySave(file: File): File(file.absolutePath) {
  private val save = ZipFile(file)
  private val params = SaveManager.params
  private val json = Json{encodeDefaults = true}
  lateinit var meta: Meta
  lateinit var userData: UserData

  init {
    kotlin.runCatching {
      meta = json.decodeFromString(Meta.serializer(), readDataAsString("meta.json")!!)
      userData = json.decodeFromString(UserData.serializer(), readDataAsString("userData.json")!!)
    }.onFailure {
      Arona.error("存档已损坏")
      throw it
    }
  }

  constructor(file: File, metaData: Meta): this(file) {
    kotlin.runCatching {
      meta = metaData
      userData = UserData(mutableListOf())
    }.onFailure {
      Arona.error("存档已损坏")
      throw it
    }
  }

  /**
   * 存储数据到存档中，entry存在则覆盖，不存在则自动新建
   * @param entry 目标文件entry
   * @param data 输入流，我不负责关闭流，调用者自己关
   */
  fun writeDataToSave(entry: String, data: InputStream): Boolean {
    kotlin.runCatching {
      params.fileNameInZip = entry
      save.removeFile(entry)
      save.addStream(data, params)
    }.onFailure {
      return false
    }

    return true
  }

  fun readDataFromSave(entry: String): InputStream? {
    val file = kotlin.runCatching { save.getFileHeader(entry) }.onFailure { it.printStackTrace() }
      .getOrNull() ?: return null
    return save.getInputStream(file)
  }

  fun readDataAsString(entry: String): String? {
    val inputStream = readDataFromSave(entry) ?: return null
    val res = String(inputStream.readBytes())
    inputStream.close()

    return res
  }

  fun readDataAsFile(entry: String, dest: File): Boolean {
    val inputStream = readDataFromSave(entry) ?: return false
    kotlin.runCatching {
      val fileOutputStream = FileOutputStream(dest)
      fileOutputStream.write(inputStream.readBytes())
      inputStream.close()
      outputStream().close()
    }.onFailure {
      it.printStackTrace()
      return false
    }

    return true
  }

  fun wipeUserData(): Boolean = kotlin.runCatching {
    userData.groups = mutableListOf()
    writeDataToSave("userData.json", json.encodeToString(UserData.serializer(), userData).byteInputStream())
    return@runCatching true
  }.getOrElse {
    return@getOrElse false
  }

  fun updateUserData(changes: UserData): Boolean {
    kotlin.runCatching {
      userData = changes
      writeDataToSave("userData.json", json.encodeToString(UserData.serializer(), userData).byteInputStream())

      return true
    }.onFailure {
      it.printStackTrace()
    }

    return false
  }
}