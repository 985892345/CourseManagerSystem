plugins {
  id("app.base.library")
  id("app.function.network")
  id("app.function.provider")
  alias(libs.plugins.kotlinx.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.courseApp.components.platform)
      implementation(projects.courseApp.components.utils)
    }
  }
}