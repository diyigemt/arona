package net.diyigemt.arona.web.database

import io.ktor.http.*
import net.diyigemt.arona.Arona
import net.diyigemt.arona.annotations.DTOService
import net.diyigemt.arona.db.BaseDTO
import net.diyigemt.arona.db.DB
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.DataBaseProvider.exec
import net.diyigemt.arona.util.ReflectionUtil
import net.diyigemt.arona.web.api.v1.message.DBJob
import net.diyigemt.arona.web.api.v1.message.ServerResponse
import net.diyigemt.arona.web.database.security.OptionFilter
import org.jetbrains.exposed.sql.FieldSet
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 *@Author hjn
 *@Create 2022/10/22
 */
object DBOptionService{
  private val tableSet : MutableSet<String> = mutableSetOf()
  suspend fun addTask(job : DBJob) : ServerResponse<out Any> =
    Arona.async {
      val db = kotlin.runCatching { DB.valueOf(job.properties.db.uppercase()) }.getOrElse {
        return@async ServerResponse(
          HttpStatusCode.NotFound.value,
          "DB: ${job.properties.db} not found",
          null as String?
        )
      }

      val tables = runCatching {
        "SELECT name _id FROM sqlite_master WHERE type ='table'".exec(db.ordinal) { resultSet ->
          resultSet.getString("_id")
        }
      }.getOrElse {
        it.printStackTrace()
        return@async ServerResponse(
          HttpStatusCode.InternalServerError.value,
          HttpStatusCode.InternalServerError.description,
          null as String?
        )
      }
      if (!tables.contains(job.properties.table))
        return@async ServerResponse(
          HttpStatusCode.NotFound.value,
          "Table: ${job.properties.table} not found in $db",
          null as String?
        )
      val tmp = OptionFilter.optionCheck(job)
      if (tmp.code != 200) return@async tmp

      val targetInstance = runCatching {
        val className = tableSet.find {
          it.contains(job.properties.table)
        }!!
        Class.forName(className).kotlin
      }.getOrElse {
        return@async ServerResponse(
          HttpStatusCode.InternalServerError.value,
          HttpStatusCode.InternalServerError.description,
          null as String?
        )
      }

      val resInstance = runCatching {
        targetInstance.nestedClasses.find { it.simpleName == job.properties.table + "DTO" }!!
      }.getOrElse {
        return@async ServerResponse(
          HttpStatusCode.InternalServerError.value,
          "No correspond DTO found",
          null as String?
        )
      }.createInstance() as BaseDTO<*>

      Arona.info(job.properties.task.args.toString())

      val query = runCatching {
        when(job.properties.task.strategy.uppercase()){
          "SELECT" -> selectAll(targetInstance, db)
          "INSERT" -> {
            var rows = ""
            var values = ""

            job.properties.task.args?.forEach { entry ->
              rows += (entry.key + ",")
              values += entry.value?.let {
                when(it::class){
                  String::class -> {
                    if (it as String == "") "''," else "'$it',"
                  }
                  else -> "$it,"
                }
              } ?: "null,"
            }.apply {
              rows = rows.substring(0, rows.length - 1)
              values = values.substring(0, values.length - 1)
            }

            runCatching {
              "INSERT INTO ${job.properties.table} ($rows) VALUES ($values)".exec(db.ordinal){
                
              }
            }.onFailure {
              it.printStackTrace()
              return@async ServerResponse(HttpStatusCode.BadRequest.value, it.localizedMessage, null as String?)
            }

            selectAll(targetInstance, db)
          }

          "UPDATE" -> {
            runCatching {
              "UPDATE ${job.properties.table} SET nameCn='update' WHERE Id=9".exec(db.ordinal){}
            }.onFailure {
              it.printStackTrace()
              return@async ServerResponse(HttpStatusCode.BadRequest.value, it.localizedMessage, null as String?)
            }

            selectAll(targetInstance, db)
          }

          else -> return@async ServerResponse(HttpStatusCode.BadRequest.value, "Undefined process strategy: ${job.properties.task.strategy}", null as String?)
        }
      }.getOrThrow()

      return@async ServerResponse(HttpStatusCode.OK.value, HttpStatusCode.OK.description, resInstance.toModel(query))
    }.await()

  private fun selectAll(instance : KClass<out Any>, db : DB) : List<ResultRow> = DataBaseProvider.query(db.ordinal) {
    Query(instance.objectInstance as FieldSet, null).toList()
  } ?: mutableListOf()

  fun init(){
    val clazz = ReflectionUtil.getTypeAnnotatedClass(DTOService::class.java)
    tableSet.addAll(clazz)
  }
}