package net.diyigemt.arona.web.database

/**
 *@Author hjn
 *@Create 2022/10/22
 */
enum class StatusCode(val code: Int, val description: String) {
  TABLE_NOT_FOUND(404, "Table not found")
}