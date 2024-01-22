plugins {
  id("app.base.library")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.courseApp.components.utils)
      api(projects.courseShared.app)
      api(libs.ktor.core)
    }
  }
}