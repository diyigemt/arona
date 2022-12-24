package net.diyigemt.arona.annotations

/**
 * 通过此注解获得该类在GlobalConfig中对应的key前缀
 */
@Target(AnnotationTarget.CLASS)
annotation class ConfigPrefix(
  val value: String
)
