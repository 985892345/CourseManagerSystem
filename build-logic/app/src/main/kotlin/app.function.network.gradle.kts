import extensions.libsLibrary
import utils.kspMultiplatform

plugins {
  id("com.google.devtools.ksp")
  id("app.base.multiplatform")
  id("io.github.qdsfdhvh.ktor-fit-plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libsLibrary("ktor-fit-annotation"))
      implementation(libsLibrary("ktor-core"))
      implementation(libsLibrary("ktor-json"))
      implementation(libsLibrary("ktor-contentNegotiation"))
    }
    val desktopMain by getting
    desktopMain.dependencies {
      implementation(libsLibrary("ktor-engines-okhttp"))
    }
    androidMain.dependencies {
      implementation(libsLibrary("ktor-engines-okhttp"))
    }
    iosMain.dependencies {
      implementation(libsLibrary("ktor-engines-darwin"))
    }
  }
}

dependencies {
  val ktorfitKsp = libsLibrary("ktor-fit-ksp")
  kspMultiplatform(ktorfitKsp, false)
}
