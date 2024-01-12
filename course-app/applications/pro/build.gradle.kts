plugins {
  id("app.base.application")
}

composeApplication {
  config(
    versionCode = 1,
    versionName = "1.0.0",
    desktopMainClass = "MainKt",
  )
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.courseApp.pages.course)
    }
  }
}

