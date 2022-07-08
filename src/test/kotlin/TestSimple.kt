package org.example.mirai.plugin

import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.Calendar

class TestSimple {

  @Test
  fun testParseDate() {
    val parse = SimpleDateFormat("yyyy/M/d HH").parse("2022/12/12 12")
    println(parse)
  }

  @Test
  fun testDateAdd() {
    val instance = Calendar.getInstance()
    instance.set(Calendar.DAY_OF_MONTH, 28)
    instance.set(Calendar.MONTH, 5)
    instance.set(Calendar.DAY_OF_MONTH, instance.get(Calendar.DAY_OF_MONTH) + 6)
    println(instance.get(Calendar.DAY_OF_MONTH))
  }

  @Test
  fun testParseInt() {
    try {
        "123asd".toInt()
    } catch (_: Exception) {
      println(123)
    }
  }

  @Test
  fun testEnumClass() {
    println(A.B in (A.C .. A.E))
  }

  enum class A {
    A, B, C, D, E
  }

}