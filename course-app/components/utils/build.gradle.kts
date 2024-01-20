plugins {
  id("app.base.library")
  id("app.function.provider")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.courseApp.components.platform)
      implementation(libs.ktProvider.manager)
    }
  }
}