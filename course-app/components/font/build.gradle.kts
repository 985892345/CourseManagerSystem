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


// font 模块需要将 res 定向为 src/commonMain/resources，这与其他模块的设置不相同
android {
  sourceSets["main"].res.srcDirs("src/commonMain/resources")
  sourceSets["main"].resources.srcDirs("src/androidMain/resources")
}