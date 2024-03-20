plugins {
  id("shared.base.multiplatform")
  alias(libs.plugins.kotlinx.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlinx.serialization)
      implementation(libs.kotlinx.datetime)
      api(projects.courseShared)
    }
  }
}