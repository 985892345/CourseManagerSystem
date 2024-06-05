import utils.kspMultiplatform

plugins {
  id("com.google.devtools.ksp")
  id("app.base.multiplatform")
  id("io.github.qdsfdhvh.ktor-fit-plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libsEx.`ktor-fit-annotation`)
      implementation(libsEx.`ktor-core`)
      implementation(libsEx.`ktor-json`)
      implementation(libsEx.`ktor-contentNegotiation`)
    }
    val desktopMain by getting
    desktopMain.dependencies {
      implementation(libsEx.`ktor-engines-okhttp`)
    }
    androidMain.dependencies {
      implementation(libsEx.`ktor-engines-okhttp`)
    }
    iosMain.dependencies {
      implementation(libsEx.`ktor-engines-darwin`)
    }
  }
}

dependencies {
  kspMultiplatform(project, libsEx.`ktor-fit-ksp`)
}
