import utils.Config

plugins {
  id("kotlin-multiplatform")
}

kotlin {
  jvm()
  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = Config.getBaseName(project)
      isStatic = true
    }
  }
//  @Suppress("OPT_IN_USAGE")
//  wasmJs {
//    browser()
//  }
}