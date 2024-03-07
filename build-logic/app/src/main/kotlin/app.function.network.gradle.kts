import extensions.libsLibrary
import utils.kspMultiplatform

plugins {
  id("com.google.devtools.ksp")
  id("app.base.multiplatform")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libsLibrary("ktor-fit-annotation"))
      implementation(rootProject.project("course-app:functions:network:api"))
    }
  }
}

dependencies {
  val ktorfitKsp = libsLibrary("ktor-fit-ksp")
  kspMultiplatform(ktorfitKsp, false)
}
