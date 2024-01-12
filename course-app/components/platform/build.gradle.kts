plugins {
  id("app.base.library")
}

kotlin {
  sourceSets {
    androidMain.dependencies {
      implementation(libs.androidWheel.extensions.android)
    }
    commonMain.dependencies {
      api(libs.kotlinx.datetime)
      api(libs.kotlinx.serialization)
      api(libs.multiplatform.settings)
    }
    desktopMain.dependencies {
    }
    iosMain.dependencies {
    }
  }
}