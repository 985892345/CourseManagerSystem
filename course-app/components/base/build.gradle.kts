plugins {
  id("app.base.library")
  id("app.function.compose")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.courseApp.components.utils)
      api(libs.bundles.navigator)
      api(libs.constraintLayout)
    }
  }
}