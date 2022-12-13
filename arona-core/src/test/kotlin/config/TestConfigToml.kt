package org.example.mirai.plugin.config

import com.akuleshov7.ktoml.Toml
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import java.io.File

class TestConfigToml {

  @Serializable
  data class TarotConfig(
    val day: Boolean
  )

  @Serializable
  data class GlobalConfig(
    val qq: Long,
    val tarot: Map<Long, TarotConfig>
  )

  @Test
  fun testParseMutableConfig() {
    val text = File("./test.toml").readText(Charsets.UTF_8)
    val config = Toml.decodeFromString(GlobalConfig.serializer(), text)
    println(config.qq)
    println(config.tarot)
  }

}