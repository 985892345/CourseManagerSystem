plugins {
  id("app.base.library")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.courseApp.components.platform)
      api(projects.courseShared.app)
      api(libs.ktor.content.negotiation)
      api(libs.ktor.json)
    }
    desktopMain.dependencies {
      implementation(libs.ktor.engines.okhttp)
    }
    androidMain.dependencies {
      implementation(libs.ktor.engines.okhttp)
    }
    iosMain.dependencies {
      implementation(libs.ktor.engines.darwin)
    }
  }
}
