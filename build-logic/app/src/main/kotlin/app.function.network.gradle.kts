import extensions.libsLibrary
import extensions.libsVersion

plugins {
  com.google.devtools.ksp
  id("app.base.multiplatform")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libsLibrary("ktor-fit-annotation"))
      implementation(project("course-app:components:network"))
    }
  }
}

dependencies {
  val ktorfitKsp = libsVersion("ktor-fit-ksp")
  add("kspAndroid", ktorfitKsp)
  add("kspDesktop", ktorfitKsp)
  add("kspIosX64", ktorfitKsp)
  add("kspIosArm64", ktorfitKsp)
  add("kspIosSimulatorArm64", ktorfitKsp)
//  add("kspWasmJs", ktorfitKsp)
}
