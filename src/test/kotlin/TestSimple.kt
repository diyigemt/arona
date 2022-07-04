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
  fun testSubstring() {
    val value = ((0).toFloat() / 20).toString()
    println(value.substring(0, value.indexOf(".") + 3))
  }

  @Test
  fun testDateAdd() {
    val instance = Calendar.getInstance()
    instance.set(Calendar.DAY_OF_MONTH, 28)
    instance.set(Calendar.MONTH, 5)
    instance.set(Calendar.DAY_OF_MONTH, instance.get(Calendar.DAY_OF_MONTH) + 6)
    println(instance.get(Calendar.DAY_OF_MONTH))
  }

}