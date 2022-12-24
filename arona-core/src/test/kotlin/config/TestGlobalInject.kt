package org.example.mirai.plugin.config

import net.diyigemt.arona.config.AronaConfig
import net.mamoe.mirai.console.data.ValueDescription
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

class TestGlobalInject {


  @Test
  fun testReadMap() {
    AronaConfig::class.declaredMemberProperties.filter {
      it.hasAnnotation<ValueDescription>()
    }.map {
      it.name
    }
  }

}