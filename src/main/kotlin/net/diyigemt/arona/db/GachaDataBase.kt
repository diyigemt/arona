package net.diyigemt.arona.db

import net.diyigemt.arona.command.cache.GachaCache
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.gacha.GachaCharacterTable
import net.diyigemt.arona.db.gacha.GachaHistoryTable
import net.diyigemt.arona.db.gacha.GachaPoolCharacterTable
import net.diyigemt.arona.db.gacha.GachaPoolTable
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
        TarotTable,
        TarotRecordTable)
    }
    GachaCache.init()
  }

}