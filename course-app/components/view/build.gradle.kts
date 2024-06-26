plugins {
  id("app.base.library")
  id("app.function.compose")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.courseApp.components.base)
      implementation(projects.courseApp.components.utils)
      implementation(projects.courseApp.components.font)
    }
  }
}