package org.example.mirai.plugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.junit.jupiter.api.Test
import kotlin.reflect.full.createType

/**
 * @description
 * @author diyigemt
 * @date 2022/9/27 12:11
 */
class TestGenericJson {

  @Serializable
  data class TestData(
    val name: String
  )

  @Test
  fun testJsonParse() {
    val se = serializer(TestData::class.createType())
    val td = Json.decodeFromString(se, "{\"name\":\"hahaha\"}")
    println((td as TestData).name)
  }

}