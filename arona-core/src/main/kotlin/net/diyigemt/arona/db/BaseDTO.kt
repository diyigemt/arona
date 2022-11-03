package net.diyigemt.arona.db

import org.jetbrains.exposed.sql.ResultRow

/**
 *@Author hjn
 *@Create 2022/10/25
 */
interface BaseDTO<T> {
  fun toModel(results : List<ResultRow>) : List<T>
}