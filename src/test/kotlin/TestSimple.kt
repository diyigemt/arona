package org.example.mirai.plugin

import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat

class TestSimple {

  @Test
  fun testParseDate() {
    val parse = SimpleDateFormat("yyyy/M/d HH").parse("2022/12/12 12")
    println(parse)
  }

}