package net.diyigemt.arona.db

import net.diyigemt.arona.command.cache.GachaCache
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.gacha.*
import net.diyigemt.arona.db.name.TeacherNameTable
import net.diyigemt.arona.db.tarot.TarotRecordTable
import net.diyigemt.arona.db.tarot.TarotTable
import org.jetbrains.exposed.sql.SchemaUtils

object GachaDataBase {

  fun init() {
    query {
      SchemaUtils.create(
        GachaCharacterTable,
        GachaPoolTable,
        GachaPoolCharacterTable,
        GachaHistoryTable,
        GachaLimitTable,
        TarotTable,
        TarotRecordTable,
        TeacherNameTable
      )
    }
    GachaCache.init()
  }

}