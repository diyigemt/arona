package net.diyigemt.arona.util

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 *@Author hjn
 *@Create 2022/10/22
 */
object MoshiUtil {
  val reflect: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

  val builtIn: Moshi = Moshi.Builder().build()

  fun custom(adapter: Any): Moshi = Moshi.Builder().add(adapter).addLast(KotlinJsonAdapterFactory()).build()
}