plugins {
  id("app.base.library")
  id("app.function.compose")
  id("app.function.serialization")
  id("app.function.navigator")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.courseApp.components.utils)
      api(libs.constraintLayout)
    }
  }
}