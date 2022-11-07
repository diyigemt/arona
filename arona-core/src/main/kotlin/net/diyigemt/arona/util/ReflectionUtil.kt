package net.diyigemt.arona.util

import net.diyigemt.arona.Arona
import org.reflections.ReflectionUtils
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.io.File
import java.net.URL
import kotlin.reflect.KClass

/**
 *@Author hjn
 *@Create 2022/10/30
 */
object ReflectionUtil : ReflectionUtils(){

  /**
   * Arona插件位置，用于设置扫描目标*/
  private val aronaUrl: URL = ClasspathHelper.forJavaClassPath().first().let {
    File(it.path.replace("%20", " ").replace("/mcl.jar", "/plugins").substring(1))
      .listFiles()!!.find { fileSet -> fileSet.name.contains("arona-arona")}!!.toURI().toURL()
  }

  /**
   * 扫描默认配置
   * 扫描范围是整个插件（不包括依赖，除非你把依赖打包到jar里了）
   * 扫描器是Type型注解和子类关系
   * 如需DIY配置属性，新建变量并使用该变量赋值后再调用方法修改*/
  val defaultConfig: ConfigurationBuilder = ConfigurationBuilder().forPackage("net.diyigemt.arona")
    .setUrls(aronaUrl)
    .setScanners(Scanners.TypesAnnotated, Scanners.SubTypes)

  /**
   * 标准扫描结果
   * 使用默认配置扫描整个插件范围内的所有class，符合需求可以直接使用该结果通过注解或子类关系筛选你想要的类*/
  val reflections = Reflections(defaultConfig)

  /**
   * 获取目标注解过的类路径
   * 本方法因轮子自带的强转方法容易失败故不负责反射，自己用Class.forName()搞去*/
  fun <T : Annotation> getTypeAnnotatedClass(annotation: Class<T>, config : ConfigurationBuilder = defaultConfig) : Set<String> {
    return if(config == defaultConfig) reflections.get(Scanners.TypesAnnotated.with(annotation))
    else Reflections(config).get(Scanners.TypesAnnotated.with(annotation))
  }

  /**
   * 获取继承接口的类路径
   * 本方法因轮子自带的强转方法容易失败故不负责反射，自己用Class.forName()搞去*/
  fun <T : Any> getInterfacePetClass(clazz: Class<T>, config : ConfigurationBuilder = defaultConfig) : Set<String> {
    return if(config == defaultConfig) reflections.get(Scanners.SubTypes.with(clazz))
    else Reflections(config).get(Scanners.SubTypes.with(clazz))
  }

  /**
   * 获取继承接口/类的单列类
   * 本方法因轮子自带的强转方法容易失败故不负责反射，自己用Class.forName()搞去*/
  @Suppress("UNCHECKED_CAST")
  inline fun <reified T> getInterfacePetObjectInstance(config : ConfigurationBuilder = defaultConfig) : List<T> {
    val paths = if(config == defaultConfig) reflections.get(Scanners.SubTypes.with(T::class.java))
    else Reflections(config).get(Scanners.SubTypes.with(T::class.java))
    kotlin.runCatching {
      return paths.mapNotNull {
        Class.forName(it).kotlin.objectInstance
      } as List<T>
    }.onFailure {
      Arona.error { it.stackTraceToString() }
    }
    return listOf()
  }
}