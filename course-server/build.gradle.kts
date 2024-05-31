import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kotlinx.serialization)
  application
}

group = "com.course.server"
version = "1.0.0"

application {
  mainClass.set("com.course.server.ApplicationKt")
}

dependencies {
  implementation(libs.spring.boot.starter)
  implementation(libs.spring.boot.starter.web)
  implementation(libs.spring.boot.druid)
  implementation(libs.mysql.connection)
  implementation(libs.mybatisPlus)
  implementation(libs.jjwt.api)
  runtimeOnly(libs.jjwt.impl)
  implementation(libs.jjwt.jackson)

  implementation(libs.kotlinx.serialization)

  implementation(projects.courseShared)
  implementation(projects.courseSource.app)
}

tasks.withType<KotlinCompile> {
  compilerOptions {
    freeCompilerArgs.add("-Xjsr305=strict")
    jvmTarget.set(JvmTarget.JVM_17)
  }
}