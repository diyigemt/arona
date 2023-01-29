package net.diyigemt.arona.web.blockly

import kotlinx.serialization.json.Json
import net.diyigemt.arona.Arona
import net.diyigemt.arona.util.MoshiUtil
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.util.Zip4jConstants
import java.io.File
import java.util.*

/**
 *@Author hjn
 *@Create 2023/1/1
 */
object SaveManager {
  private val saveFolder = File(Arona.dataFolder.absolutePath, "blocklySave")
  private val json = Json{encodeDefaults = true}
  val saves: MutableList<BlocklySave> = mutableListOf()
  /**
   * 使用者需要复制该变量并设置fileNameInZip，不确定后果时不要修改任何其它成员变量，否则可能导致ZIP损坏*/
  val params = ZipParameters()

  fun init() {
    saveFolder.mkdirs()
    params.compressionMethod = Zip4jConstants.COMP_STORE
    params.compressionLevel = Zip4jConstants.DEFLATE_LEVEL_NORMAL
    params.isSourceExternalStream = true
    loadLocalSave()
  }

  private fun newSave(meta: Meta): BlocklySave? {
    val fileName = let {
      var res: String
      val list = kotlin.runCatching { saveFolder.listFiles()!! }.getOrElse {
        Arona.error("Can not read folder, Permission denied")
        return null
      }

      do {
        res = UUID.randomUUID().toString()
      }while (list.find { it.name == res } != null)

      return@let res
    }

    val file = File(saveFolder.absolutePath, "$fileName.ara")
    val tmp = Meta(meta.version, meta.projectName, meta.resPath, UUID.fromString(fileName))

    kotlin.runCatching {
      val res = ZipFile(file)
      val listFiles = arrayListOf("resources/", "blocklyProject.json", "expression.json", "meta.json", "userData.json")

      for (item in listFiles) {
        params.fileNameInZip = item
        res.addStream(
          when(item){
            // TODO: 需要写初始数据的在这里写
            "meta.json" -> json.encodeToString(Meta.serializer(), tmp).byteInputStream()
            "userData.json" -> json.encodeToString(
              UserData.serializer(),
              UserData(mutableListOf())
            ).byteInputStream()
            else -> "".byteInputStream()
          }, params
        )
      }
      params.fileNameInZip = ""
    }.onFailure {
      it.printStackTrace()
      Arona.error("新建存档失败")
      return null
    }

    return kotlin.runCatching {
      BlocklySave(file, tmp)
    }.onFailure {
      it.printStackTrace()
      file.delete()
    }.getOrThrow()
  }

  private fun loadLocalSave(){
    kotlin.runCatching {
      saveFolder.listFiles()!!.filter { it.extension == "ara" }.forEach {
        loadSaveFromLocal(it)
      }
    }.onFailure {
      it.printStackTrace()
      Arona.error("Permission denied")
    }
  }

  private fun loadSaveFromLocal(file: File): Boolean{
    kotlin.runCatching {
      val localSave = BlocklySave(file)
      BlocklyService.updateTriggers(
        localSave.meta.uuid,
        MoshiUtil.reflect.adapter(BlocklyExpression::class.java).fromJson(localSave.readDataAsString("expression.json")!!)!!
      )
      saves.add(localSave)
      Arona.info("触发器：${localSave.meta.projectName}, 加载成功")
    }.onFailure {
      it.printStackTrace()
      return false
    }

    return true
  }

  fun addSaveFromRemote(data: CommitData): UUID? {
    val newSave = newSave(Meta(projectName = data.projectName))?: return null
    newSave.writeDataToSave(
      "expression.json", MoshiUtil.reflect.adapter(BlocklyExpression::class.java)
      .toJson(data.trigger).byteInputStream()
    ).apply {
      if (!this){
        newSave.delete()
        return null
      }
    }

    newSave.writeDataToSave("BlocklyProject.json", data.blocklyProject.byteInputStream()).apply {
      if (!this){
        newSave.delete()
        return null
      }
    }

    newSave.writeDataToSave("userData.json", data.userData.byteInputStream()).apply {
      if (!this){
        newSave.delete()
        return null
      }
    }
    // TODO: 资源文件处理
    saves.add(newSave)

    return newSave.meta.uuid
  }

  fun updateSaveFromRemote(data: CommitData): Boolean{
    val save = let{
      saves.forEach {
        if (json.decodeFromString(Meta.serializer(), it.readDataAsString("meta.json")!!).uuid == data.uuid){
          return@let it
        }
      }
      return@let null
    } ?: return false

    save.writeDataToSave(
      "expression.json", MoshiUtil.reflect.adapter(BlocklyExpression::class.java)
        .toJson(data.trigger).byteInputStream()
    ).apply {
      if (!this){
        return false
      }
    }

    save.writeDataToSave("BlocklyProject.json", data.blocklyProject.byteInputStream()).apply {
      if (!this){
        return false
      }
    }

    save.writeDataToSave("userData.json", data.userData.byteInputStream()).apply {
      if (!this){
        return false
      }
    }

    return true
  }

  fun deleteSaveFormRemote(data: CommitData): Boolean {
    saves.forEach {
      if (it.meta.uuid == data.uuid) {
        kotlin.runCatching { it.delete() }.onFailure { e ->
          e.printStackTrace()
          return false
        }.onSuccess { _ ->
          saves.remove(it)
          return true
        }
      }
    }

    return false
  }

  fun listOfSaves(): List<ListSaves>{
    val res: MutableList<ListSaves> = mutableListOf()
    saves.forEach{
      kotlin.runCatching {
        val project = it.readDataAsString("BlocklyProject.json")!!
        val userData = it.readDataAsString("userData.json")!!
        res.add(ListSaves(it.meta.projectName,it.meta.uuid , project, userData))
      }
    }

    return res
  }

  /**
   * @return 模板类型有: Meta UserData*/
  inline fun <reified T: SaveDataElement> getSaveElementByUUID(uuid: UUID): T? {
    saves.forEach {
      if(it.meta.uuid == uuid) return when(T::class.java) {
        Meta::class.java -> it.meta as? T
        UserData::class.java -> it.userData as? T
        else -> {
          Arona.error("Undefined element type: ${T::class.java.name}")
          null
        }
      }
    }

    return null
  }
}