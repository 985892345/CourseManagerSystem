plugins {
  id("app.base.library")
  id("app.function.compose")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.constraintLayout)
    }
  }
}