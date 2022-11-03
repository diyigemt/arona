package net.diyigemt.arona.web.database

import io.ktor.http.*
import kotlinx.coroutines.coroutineScope
import net.diyigemt.arona.Arona
import net.diyigemt.arona.annotations.DTOService
import net.diyigemt.arona.db.BaseDTO
import net.diyigemt.arona.db.DB
import net.diyigemt.arona.db.DataBaseProvider
import net.diyigemt.arona.db.DataBaseProvider.exec
import net.diyigemt.arona.db.data.schaledb.Raid
import net.diyigemt.arona.web.api.v1.message.DBJob
import net.diyigemt.arona.web.api.v1.message.ServerResponse
import net.diyigemt.arona.web.database.security.OptionFilter
import org.jetbrains.exposed.sql.selectAll
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.io.File
import kotlin.reflect.full.createInstance

/**
 *@Author hjn
 *@Create 2022/10/22
 */
object DBOptionService {
  private val tableSet: MutableSet<String> = mutableSetOf()
  suspend fun addTask(job: DBJob): ServerResponse<out Any> =
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
      val resInstance = runCatching {
        val className = tableSet.find {
          it.contains(job.properties.table + "DTO")
        }!!
        Class.forName(className).kotlin
      }.getOrElse {
        return@async ServerResponse(HttpStatusCode.NotFound.value, "No correspond DTO found", null as String?)
      }.createInstance() as BaseDTO<*>
      val query = runCatching {
        when (job.properties.task.strategy) {
          "SELECT" -> {
            DataBaseProvider.query(db.ordinal) {
              Raid.selectAll().toList()
            } ?: mutableListOf()
          }

          else -> return@async ServerResponse(
            HttpStatusCode.BadRequest.value,
            "Undefined process strategy",
            null as String?
          )
        }
      }.getOrThrow()
      return@async ServerResponse(HttpStatusCode.OK.value, HttpStatusCode.OK.description, resInstance.toModel(query))
    }.await()

  fun init() {
    val url = ClasspathHelper.forJavaClassPath().first().let {
      File(it.path.replace("%20", " ").replace("/mcl.jar", "/plugins").substring(1))
        .listFiles()!!.find { fileSet -> fileSet.name.contains("arona-arona") }!!.toURI().toURL()
    }

    val config = ConfigurationBuilder().forPackage("net.diyigemt.arona.db")
      .setUrls(url)
      .setScanners(Scanners.TypesAnnotated)

    val clazz = Reflections(config).get(Scanners.TypesAnnotated.with(DTOService::class.java))
    tableSet.addAll(clazz)
  }
}