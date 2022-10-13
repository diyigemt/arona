package net.diyigemt.arona.db.data.schaledb

import net.diyigemt.arona.Arona
import net.diyigemt.arona.db.DB
import net.diyigemt.arona.db.DataBaseProvider
import org.jetbrains.exposed.sql.SchemaUtils

/**
 *@Author hjn
 *@Create 2022/8/26
 */
object SchaleDataBase {
  fun init(){
    kotlin.runCatching {
      initDataBase()
    }.onFailure {
      Arona.warning(it.toString())
      Arona.warning("数据库修改操作失败，删除当前库重建")
      deleteDataBase()
      initDataBase()
    }
  }

  private fun initDataBase(){
    DataBaseProvider.query(DB.DATA.ordinal) {
      SchemaUtils.createMissingTablesAndColumns(
        MD5,

        Students,
        Localization,
        Raid,
        CurrentData
      )
    }
  }

  private fun deleteDataBase(){
    DataBaseProvider.query(DB.DATA.ordinal) {
      SchemaUtils.drop(
        MD5,

        Students,
        Localization,
        Raid,
        CurrentData)
    }
  }
}