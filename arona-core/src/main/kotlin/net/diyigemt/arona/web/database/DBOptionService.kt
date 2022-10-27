package net.diyigemt.arona.web.database

import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.diyigemt.arona.annotations.DTOService
import net.diyigemt.arona.db.DB
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.DataBaseProvider.exec
import net.diyigemt.arona.db.data.schaledb.Raid
import net.diyigemt.arona.entity.dto.BaseDTO
import net.diyigemt.arona.web.api.v1.message.DBJob
import net.diyigemt.arona.web.api.v1.message.ServerResponse
import net.diyigemt.arona.web.database.security.OptionFilter
import org.jetbrains.exposed.sql.selectAll
import org.reflections.Reflections
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.full.createInstance

/**
 *@Author hjn
 *@Create 2022/10/22
 */
object DBOptionService : CoroutineScope{
  private val tableSet : MutableSet<Class<*>> = mutableSetOf()
  suspend fun addTask(job : DBJob) : ServerResponse<out Any> = coroutineScope {
    async {
      val db = kotlin.runCatching { DB.valueOf(job.properties.db.uppercase()) }.getOrElse {
        return@async ServerResponse(HttpStatusCode.NotFound.value, "DB: ${job.properties.db} not found", null as String?)
      }

      val tables = runCatching {
        "SELECT name _id FROM sqlite_master WHERE type ='table'".exec(db.ordinal) { resultSet ->
          resultSet.getString("_id")
        }
      }.getOrElse {
        it.printStackTrace()
        return@async ServerResponse(HttpStatusCode.InternalServerError.value, HttpStatusCode.InternalServerError.description, null as String?)
      }

      if(!tables.contains(job.properties.table))
        return@async ServerResponse(HttpStatusCode.NotFound.value, "Table: ${job.properties.table} not found in $db", null as String?)

      val tmp = OptionFilter.optionCheck(job)
      if (tmp.code != 200) return@async tmp

      val query = run {
        DataBaseProvider.query(db.ordinal) {
          Raid.selectAll().toList()
        } ?: mutableListOf()
      }

      val resInstance = runCatching {
        Class.forName("net.diyigemt.arona.entity.dto.${job.properties.db}.${job.properties.table}").kotlin
      }.getOrElse {
        return@async ServerResponse(HttpStatusCode.NotFound.value, "No correspond DTO found", null as String?)
      }.createInstance() as BaseDTO<*>

      return@async ServerResponse(HttpStatusCode.OK.value, HttpStatusCode.OK.description, resInstance.toModel(query))
    }.await()
  }

  fun init(){
    val clazz = Reflections("net.diyigemt.arona.db").getTypesAnnotatedWith(DTOService::class.java)
    tableSet.addAll(clazz)
    clazz.forEach{
      it.declaredClasses
    }
  }

  override val coroutineContext: CoroutineContext
    get() = TODO("Not yet implemented")
}