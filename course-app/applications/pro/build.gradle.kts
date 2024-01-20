plugins {
  id("app.base.application")
  id("app.function.provider")
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
      implementation(projects.courseApp.components.base)
      implementation(projects.courseApp.components.utils)
      implementation(projects.courseApp.pages.course)
      implementation(libs.voyager.navigator)
      implementation(libs.voyager.screenmodel)
      implementation(libs.voyager.transitions)
      implementation(libs.voyager.tabNavigator)
      implementation(libs.voyager.bottomSheetNavigator)
    }
  }
}

