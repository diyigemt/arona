package net.diyigemt.arona.db

import net.diyigemt.arona.command.cache.GachaCache
import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.db.announcement.RemoteActionTable
import net.diyigemt.arona.db.gacha.*
import net.diyigemt.arona.db.image.ImageTable
import net.diyigemt.arona.db.name.GameNameTable
import net.diyigemt.arona.db.name.TeacherNameTable
import net.diyigemt.arona.db.system.SystemTable
import net.diyigemt.arona.db.tarot.TarotRecordTable
import net.diyigemt.arona.db.tarot.TarotTable
import net.diyigemt.arona.util.ReflectionUtil
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table

object BaseDataBase {

  fun init() {
    query {
      SchemaUtils.create(*ReflectionUtil.getInterfacePetObjectInstance<Table>().toTypedArray())
    }
    GachaCache.init()
  }

}