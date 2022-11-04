package net.diyigemt.arona.annotations

/**
 * 在自动注册控制台权限时默认隐藏
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HideService
