package net.diyigemt.arona.db

import net.diyigemt.arona.command.cache.GachaCache
import net.diyigemt.arona.db.gacha.GachaCharacterTable
import net.diyigemt.arona.db.gacha.GachaHistoryTable
import net.diyigemt.arona.db.model.gacha.GachaPoolCharacterTable
import net.diyigemt.arona.db.gacha.GachaPoolTable
import org.jetbrains.exposed.sql.SchemaUtils

object GachaDataBase {

  fun init() {
    SchemaUtils.create(GachaCharacterTable, GachaPoolTable, GachaPoolCharacterTable, GachaHistoryTable)
    GachaCache.init()
  }

}