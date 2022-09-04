package net.diyigemt.arona.db.data.schaledb

import net.diyigemt.arona.db.DB
import net.diyigemt.arona.db.DataBaseProvider
import org.jetbrains.exposed.sql.SchemaUtils

/**
 *@Author hjn
 *@Create 2022/8/26
 */
object SchaleDataBase {
  fun init(){
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
}