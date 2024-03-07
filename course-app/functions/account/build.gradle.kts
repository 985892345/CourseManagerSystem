plugins {
  id("app.base.library")
  id("app.function.network")
  id("app.function.provider")
  id("app.function.serialization")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.courseApp.functions.account.api)
      implementation(projects.courseApp.components.utils)
    }
  }
}