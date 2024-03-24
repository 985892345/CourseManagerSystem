plugins {
  id("app.base.library")
  id("app.function.compose")
  id("app.function.provider")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.courseApp.components.utils)
    }
  }
}

android {
  sourceSets["main"].res.srcDirs("src/commonMain/resources")
  sourceSets["main"].resources.srcDirs("src/androidMain/resources")
}