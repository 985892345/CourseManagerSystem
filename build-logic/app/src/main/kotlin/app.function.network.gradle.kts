import extensions.libsLibrary

plugins {
  com.google.devtools.ksp
  id("app.base.multiplatform")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libsLibrary("ktor-fit-annotation"))
      implementation(rootProject.project("course-app:functions:network"))
    }
  }
}

dependencies {
  val ktorfitKsp = libsLibrary("ktor-fit-ksp")
  add("kspAndroid", ktorfitKsp)
  add("kspDesktop", ktorfitKsp)
  add("kspIosX64", ktorfitKsp)
  add("kspIosArm64", ktorfitKsp)
  add("kspIosSimulatorArm64", ktorfitKsp)
//  add("kspWasmJs", ktorfitKsp)
}
