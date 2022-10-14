package net.diyigemt.arona.plugins.action

/**
 *@Author hjn
 *@Create 2022/10/14
 */
@Suppress("UNCHECKED_CAST")
interface BaseAction<T> {
  val actionName : String

  fun actionHandler(event : T)

  fun actionInit() : Map<String, BaseAction<Any>> = mapOf(Pair(actionName, this as BaseAction<Any>))
}