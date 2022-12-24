package net.diyigemt.arona.annotations

/**
 * 通过此注解获得在GlobalConfig中对应的key
 */
@Target(AnnotationTarget.PROPERTY)
annotation class ConfigKey(
  val value: String
)
