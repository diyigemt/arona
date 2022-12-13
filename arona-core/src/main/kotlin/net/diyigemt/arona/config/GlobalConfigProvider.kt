package net.diyigemt.arona.config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import net.diyigemt.arona.interfaces.Initialize

object GlobalConfigProvider: Initialize {

  private val TomlInstance: Toml by lazy {
    Toml(
      inputConfig = TomlInputConfig(
        ignoreUnknownNames = true
      )
    )
  }

  override val priority: Int
    get() = 5

  override fun init() {

  }

}