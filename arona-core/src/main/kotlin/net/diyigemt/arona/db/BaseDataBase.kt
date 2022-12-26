package net.diyigemt.arona.db

import net.diyigemt.arona.db.DataBaseProvider.query
import net.diyigemt.arona.util.ReflectionUtil
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table

object BaseDataBase {

  fun init() {
    query {
      SchemaUtils.create(*ReflectionUtil.getInterfacePetObjectInstance<Table>().toTypedArray())
    }
  }
}