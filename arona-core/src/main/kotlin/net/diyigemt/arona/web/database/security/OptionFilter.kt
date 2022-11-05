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
  /**
   * 将你希望对某个表只执行固定操作的数据库操作加入*/
  private val tables : MutableMap<String, List<Strategy>> = mutableMapOf(
    Pair("GachaHistory", listOf(Strategy.SELECT, Strategy.DELETE)),
    Pair("GachaLimit", listOf(Strategy.SELECT, Strategy.DELETE))
  )
  fun optionCheck(job : DBJob) : ServerResponse<Any> = when{
    (tables[job.properties.table] != null &&
      !tables[job.properties.table]!!.contains(Strategy.valueOf(job.properties.task.strategy.uppercase()))) -> {
      ServerResponse(HttpStatusCode.MethodNotAllowed.value, "Strategy Not Allowed", null as String?)
    }

    else ->{ServerResponse(HttpStatusCode.OK.value, HttpStatusCode.OK.description)}
  }
}