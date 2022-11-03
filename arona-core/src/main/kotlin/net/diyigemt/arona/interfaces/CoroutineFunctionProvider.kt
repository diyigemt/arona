package net.diyigemt.arona.interfaces

import kotlinx.coroutines.*
import net.diyigemt.arona.Arona

abstract class CoroutineFunctionProvider {

  abstract val tag: String

  protected abstract suspend fun main()

  @Suppress("NOTHING_TO_INLINE")
  inline fun warning(text: String) {
    Arona.warning { "$tag: $text" }
  }
  @Suppress("NOTHING_TO_INLINE")
  inline fun error(text: String) {
    Arona.error { "$tag: $text" }
  }
  @Suppress("NOTHING_TO_INLINE")
  inline fun info(text: String) {
    Arona.info { "$tag: $text" }
  }
  @Suppress("NOTHING_TO_INLINE")
  inline fun verbose(text: String) {
    Arona.verbose { "$tag: $text" }
  }

  fun start() : Job = Arona.runSuspend { main() }

  open fun disable() {}

}