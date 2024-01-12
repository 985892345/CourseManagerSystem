import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.kotlin.spring)
  application
}

group = "com.course.server"
version = "1.0.0"

application {
  mainClass.set("com.course.server.ApplicationKt")
}

dependencies {
  implementation(libs.spring.boot.starter)
  implementation(libs.spring.boot.druid)
  implementation(libs.mysql.connection)
  implementation(libs.mybatisPlus)
  implementation(libs.jjwt.api)
  runtimeOnly(libs.jjwt.impl)

  implementation(projects.courseShared.app)
  implementation(projects.courseShared.backend)
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs += "-Xjsr305=strict"
    jvmTarget = "17"
  }
}