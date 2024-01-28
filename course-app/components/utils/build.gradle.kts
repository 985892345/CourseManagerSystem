plugins {
  id("app.base.library")
  id("app.function.compose")
  id("app.function.provider")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.ktProvider.manager)
      api(libs.kotlinx.datetime)
      api(libs.kotlinx.serialization)
      api(libs.multiplatform.settings)
      implementation(libs.multiplatform.settingsNoArg)
    }
    androidMain.dependencies {
      api(libs.androidWheel.extensions.android)
    }
  }
}