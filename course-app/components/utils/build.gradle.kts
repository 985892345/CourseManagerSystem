plugins {
  id("app.base.library")
  id("app.function.provider")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.ktProvider.manager)
      api(libs.kotlinx.datetime)
      api(libs.kotlinx.serialization)
      api(libs.multiplatform.settings)
    }
  }
}