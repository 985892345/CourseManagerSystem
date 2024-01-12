plugins {
  id("shared.base.multiplatform")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.courseShared.base)
    }
  }
}