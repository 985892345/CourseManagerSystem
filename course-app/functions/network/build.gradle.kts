plugins {
  id("app.base.library")
  id("app.function.provider")
  id("app.function.serialization")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.courseApp.functions.network.api)
      implementation(projects.courseApp.components.utils)
      implementation(libs.ktor.contentNegotiation)
      implementation(libs.ktor.json)
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
