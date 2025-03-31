plugins {
  val kotlinVersion = "1.7.10"
  val miraiVersion = "2.16.0"
  kotlin("jvm") version kotlinVersion
  kotlin("plugin.serialization") version kotlinVersion
  id("com.github.gmazzo.buildconfig") version "4.1.1"
  id("net.mamoe.mirai-console") version miraiVersion
}

group = "net.diyigemt"
version = "1.1.5"
val exposedVersion = "0.38.2"
val sqliteVersion = "3.36.0.3"
val quartzVersion = "2.3.2"
val okhttpVersion = "4.10.0"

repositories {
  mavenCentral()
  maven("https://jitpack.io")
  maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  //Overflow依赖仓库
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}
//Overflow测试需移除mirai-core
mirai {
  noTestCore = true
  setupConsoleTestRuntime {
    // 移除 mirai-core 依赖
    classpath = classpath.filter {
      !it.nameWithoutExtension.startsWith("mirai-core-jvm")
    }
  }
}
buildConfig {
  className("BuildConfig")
  packageName("net.diyigemt.arona.cg")
  buildConfigField("String", "version", "\"${version}\"")
  buildConfigField("String", "name", "\"blue-archive-arona\"")
  buildConfigField("String", "id", "\"net.diyigemt.arona\"")
}
dependencies {
//    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
  implementation("org.jsoup:jsoup:1.15.1")
  testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
  testImplementation("io.kotest:kotest-runner-junit5:5.3.0")
  testImplementation("io.kotest:kotest-assertions-core:5.3.0")
  implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
  implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
  implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
  implementation("org.xerial:sqlite-jdbc:$sqliteVersion")
  implementation("org.quartz-scheduler:quartz:$quartzVersion")
  // https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
  implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
  implementation("org.apache.logging.log4j:log4j-core:2.18.0")
  implementation("org.slf4j:slf4j-api:1.7.36")
  implementation("com.google.code.gson:gson:2.9.0")
  // https://mvnrepository.com/artifact/me.xdrop/fuzzywuzzy
  implementation("me.xdrop:fuzzywuzzy:1.4.0")
  // https://mvnrepository.com/artifact/com.github.taptap/pinyin-plus
  implementation("com.github.taptap:pinyin-plus:1.0")
  implementation("net.mamoe.yamlkt:yamlkt-jvm:0.10.2")
  //Overflow运行时依赖
  testConsoleRuntime("top.mrxiaom.mirai:overflow-core:1.0.4.589-05fbc9f-SNAPSHOT")
}

tasks.test {
  useJUnitPlatform()
}
