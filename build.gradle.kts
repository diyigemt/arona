plugins {
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.10.0"
}

group = "org.example"
version = "0.1.0"

repositories {
    maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
    mavenCentral()
}

dependencies {
//    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("org.jsoup:jsoup:1.15.1")
   testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    testImplementation("io.kotest:kotest-runner-junit5:5.3.0")
    testImplementation("io.kotest:kotest-assertions-core:5.3.0")
}

tasks.test {
  useJUnitPlatform()
}