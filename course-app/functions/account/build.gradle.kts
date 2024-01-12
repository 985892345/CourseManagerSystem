plugins {
  id("app.base.library")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.courseApp.components.platform)
    }
  }
}