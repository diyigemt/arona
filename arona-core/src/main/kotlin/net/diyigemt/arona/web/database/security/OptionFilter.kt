package net.diyigemt.arona.web.database.security

import io.ktor.http.*
import net.diyigemt.arona.web.api.v1.message.DBJob
import net.diyigemt.arona.web.api.v1.message.ServerResponse
import net.diyigemt.arona.web.database.Strategy

/**
 *@Author hjn
 *@Create 2022/10/22
 */
object OptionFilter {
  private val tables : MutableMap<String, List<Strategy>> = mutableMapOf(
    Pair("wsad", listOf(Strategy.SELECT))
  )
  fun optionCheck(job : DBJob) : ServerResponse<Any> = when{
    (tables[job.properties.table] != null &&
      !tables[job.properties.table]!!.contains(Strategy.valueOf(job.properties.task.strategy.uppercase()))) -> {
      ServerResponse(HttpStatusCode.MethodNotAllowed.value, HttpStatusCode.MethodNotAllowed.description, null as String?)
    }

    else ->{ServerResponse(HttpStatusCode.OK.value, HttpStatusCode.OK.description)}
  }
}