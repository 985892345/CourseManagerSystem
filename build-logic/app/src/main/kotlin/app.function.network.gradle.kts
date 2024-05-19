import extensions.libsLibrary
import utils.kspMultiplatform

plugins {
  id("com.google.devtools.ksp")
  id("app.base.multiplatform")
  id("io.github.qdsfdhvh.ktor-fit-plugin")
//  id("de.jensklingenberg.ktorfit") // ktorfit 因为需要父接口同时生成代码而无法使用，因为父接口在 source 模块中
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libsLibrary("ktor-fit-annotation"))
//      implementation(libsLibrary("ktorfit"))
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
//  val ktorfitKsp2 = libsLibrary("ktorfit-ksp")
//  kspMultiplatform(ktorfitKsp2)
  val ktorfitKsp = libsLibrary("ktor-fit-ksp")
  kspMultiplatform(ktorfitKsp)
}
