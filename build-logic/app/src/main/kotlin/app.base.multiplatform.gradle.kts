import utils.Config

plugins {
  `kotlin-multiplatform`
}

kotlin {
  jvm("desktop")
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
  androidTarget {
    compilations.all {
      kotlinOptions {
        jvmTarget = "1.8"
      }
    }
  }
}