package net.diyigemt.arona.interfaces

import kotlinx.coroutines.*
import net.diyigemt.arona.Arona
import kotlin.coroutines.CoroutineContext

abstract class BaseFunctionProvider(ctx: CoroutineContext? = null): CoroutineScope {

  abstract val tag: String
  final override val coroutineContext: CoroutineContext
    get() = SupervisorJob(Arona.coroutineContext.job)

  init {
    if(ctx != null) {
      coroutineContext.plus(ctx)
    }
  }

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

  fun start() : Job = this.launch(context = this.coroutineContext) { main() }

  open fun disable() {}

}