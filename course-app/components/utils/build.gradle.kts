plugins {
  id("app.base.library")
  id("app.function.compose")
  id("app.function.provider")
  id("app.function.serialization")
  id("app.function.navigator")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.ktProvider.manager)
      api(libs.kotlinx.datetime)
      api(libs.multiplatform.settings)
      api(projects.courseShared)
      api(projects.courseSource.app)
    }
    androidMain.dependencies {
      api(libs.androidWheel.extensions.android)
    }
  }
}
