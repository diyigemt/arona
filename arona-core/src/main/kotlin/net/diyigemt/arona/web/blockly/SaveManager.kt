package net.diyigemt.arona.web.blockly

import kotlinx.serialization.json.Json
import net.diyigemt.arona.Arona
import net.diyigemt.arona.interfaces.Initialize
import net.diyigemt.arona.util.MoshiUtil
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.util.Zip4jConstants
import java.io.File

/**
 *@Author hjn
 *@Create 2023/1/1
 */
object SaveManager: Initialize {
  private val saveFolder = File(Arona.dataFolder.absolutePath, "blocklySave")
  private val saves: MutableList<BlocklySave> = mutableListOf()
  /**
   * 使用者需要复制该变量并设置fileNameInZip，不确定后果时不要修改任何其它成员变量，否则可能导致ZIP损坏*/
  val params = ZipParameters()

  override fun init() {
    saveFolder.mkdirs()
    params.compressionMethod = Zip4jConstants.COMP_STORE
    params.compressionLevel = Zip4jConstants.DEFLATE_LEVEL_NORMAL
    params.isSourceExternalStream = true
    loadLocalSave()
  }

  private fun newSave(): BlocklySave? {
    val fileName = let {
      var res: String
      val list = kotlin.runCatching { saveFolder.listFiles()!! }.getOrElse {
        Arona.error("Can not read folder, Permission denied")
        return null
      }

      do {
        res = generateSecret()
      }while (list.find { it.name == res } != null)

      return@let res
    }

    val file = File(saveFolder.absolutePath, "$fileName.ara")

    kotlin.runCatching {
      val res = ZipFile(file)
      val listFiles = arrayListOf("resources/", "blocklyProject.json", "expression.json", "meta.json", "user.json")
      val json = Json{encodeDefaults = true}

      for (item in listFiles) {
        params.fileNameInZip = item
        res.addStream(
          when(item){
            // TODO: 需要写初始数据的在这里写
            "meta.json" -> json.encodeToString(Meta.serializer(), Meta()).byteInputStream()
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

    return BlocklySave(file)
  }

  private fun generateSecret(): String {
    val dictChars = mutableListOf<Char>().apply { "123456789zxcvbnmasdfghjklqwertyuiop".forEach { this.add(it) } }
    val randomStr = StringBuilder().apply { (1..10).onEach { append(dictChars.random().uppercaseChar()) } }

    return randomStr.toString()
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

  fun loadSaveFromLocal(file: File): Boolean{
    kotlin.runCatching {
      val localSave = BlocklySave(file)
      BlocklyService.addHook(
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

  fun addSaveFromRemote(data: CommitData): Boolean {
    val newSave = newSave()?: return false
    val json = Json { encodeDefaults = true }
    newSave.writeDataToSave(
      "expression.json", MoshiUtil.reflect.adapter(BlocklyExpression::class.java)
      .toJson(data.trigger).byteInputStream()
    ).apply {
      if (!this){
        newSave.delete()
        return false
      }
    }

    newSave.writeDataToSave("BlocklyProject.json", data.blocklyProject.byteInputStream()).apply {
      if (!this){
        newSave.delete()
        return false
      }
    }

    newSave.writeDataToSave(
      "meta.json", json.encodeToString(Meta.serializer(), Meta(projectName = data.projectName))
        .byteInputStream()
    ).apply {
      if (!this){
        newSave.delete()
        return false
      }
    }
    //TODO: 资源文件处理
    saves.add(newSave)

    return true
  }

  fun listOfSaves(): List<ListSaves>{
    val res: MutableList<ListSaves> = mutableListOf()
    saves.forEach{
      kotlin.runCatching {
        res.add(ListSaves(it.meta.projectName, it.readDataAsString("BlocklyProject.json")!!))
      }
    }

    return res
  }
}