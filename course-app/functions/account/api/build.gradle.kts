plugins {
  id("app.base.library")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.courseShared.base)
    }
  }
}