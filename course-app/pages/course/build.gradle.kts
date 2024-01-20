plugins {
  id("app.base.library")
  id("app.function.compose")
//  id("app.function.network")
  id("app.function.provider")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.courseApp.components.base)
    }
  }
}