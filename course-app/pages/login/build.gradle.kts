plugins {
  id("app.base.library")
  id("app.function.compose")
  id("app.function.provider")
  id("app.function.navigator")
  id("app.function.serialization")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.courseApp.components.base)
      implementation(projects.courseApp.components.utils)
      implementation(projects.courseApp.components.view)
      implementation(projects.courseApp.pages.main)
    }
  }
}