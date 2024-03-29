plugins {
  val kotlinVersion = "1.7.10"
  val miraiVersion = "2.12.3"
  kotlin("jvm") version kotlinVersion
  kotlin("plugin.serialization") version kotlinVersion

  id("net.mamoe.mirai-console") version miraiVersion

  application
  id("io.ktor.plugin") version "2.1.2"
}

group = "net.diyigemt.arona"
version = "2.0.0-M1"
val miraiVersion = "2.12.3"
val exposedVersion = "0.38.2"
val sqliteVersion = "3.36.0.3"
val quartzVersion = "2.3.2"
val okhttpVersion = "4.10.0"
val ktor_version = "2.1.2"
val kotlin_version = "1.7.10"
val logback_version = "1.2.11"

repositories {
  mavenCentral()
  maven("https://jitpack.io")
  maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
}

application{
  mainClass.set("org.diyigemt.arona.Arona")

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
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
  implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
  implementation("net.mamoe.yamlkt:yamlkt-jvm:0.10.2")
  implementation("org.reflections:reflections:0.10.2")

  implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
  implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-cors:$ktor_version")
  implementation("io.ktor:ktor-server-auth:$ktor_version")
  implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
  implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
  implementation("org.apache.commons:commons-jexl3:3.2.1")
  implementation("net.lingala:zip4j:1.3.3")
//  implementation("ch.qos.logback:logback-classic:$logback_version")
  testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

  implementation(files("C:\\Users\\Haythem Kenway\\Desktop\\mirai-console\\plugins\\MiraiPluginUpdater-0.1.0.mirai2.jar"))
}

tasks.test {
  useJUnitPlatform()
}

tasks.create("buildAndDeploy"){
  group = "mirai"
  dependsOn("buildPlugin")
  doLast {
    exec{
      workingDir("$rootDir")
      commandLine("cmd", "/c", "deploy.cmd", "${rootProject.name}-$version.mirai2.jar")
    }
  }
}
