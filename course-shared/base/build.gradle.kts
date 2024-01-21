plugins {
  id("shared.base.multiplatform")
  alias(libs.plugins.kotlinx.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.kotlinx.serialization)
    }
  }
}