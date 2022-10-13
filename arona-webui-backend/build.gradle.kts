import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.12.3"

    application
    id("io.ktor.plugin") version "2.1.2"
}

group = "net.diyigemt.arona"
version = "0.1.0"

val ktor_version = "2.1.2"
val kotlin_version = "1.6.21"
val logback_version = "1.2.11"

repositories {
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
//    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

application{
    mainClass.set("WenUI")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")
    implementation("io.ktor:ktor-server-html-builder-jvm:$ktor_version")
    implementation("org.jetbrains:kotlin-css-jvm:1.0.0-pre.129-kotlin-1.4.20")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    implementation("net.mamoe.yamlkt:yamlkt-jvm:0.10.2")

    implementation(fileTree("${rootProject.rootDir}/plugins").include("*.jar"))
}

tasks.test {
    useJUnitPlatform()
}

//tasks.create("buildAndDeploy"){
//    group = "mirai"
//    description = "build mirai2 plugin and deploy"
//    dependsOn("buildPlugin")
//    exec {
//        workingDir("${rootProject.rootDir}")
////        commandLine("cmd", "/c", "dir")
//        commandLine("cmd", "/c", "deploy.cmd", "${rootProject.name}-${version}.mirai2.jar")
//    }
//}