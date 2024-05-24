import extensions.libsLibrary
import utils.Config

plugins {
  id("kotlin-multiplatform")
}

kotlin {
  jvm("desktop")
  jvmToolchain(17)

  // 暂时注释，导入 iOS 后会导致 commonMain 源集无法使用 kotlin-stdlib 依赖
  if (project.properties["sourceSets.ios"].toString().toBooleanStrictOrNull() == true) {
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
  }
  androidTarget {
    compilations.all {
      kotlinOptions {
        jvmTarget = "17"
      }
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libsLibrary("kotlinx-coroutines"))
      implementation(libsLibrary("kotlinx-collections"))
    }
    androidMain.dependencies {
      implementation(libsLibrary("kotlinx-coroutinesAndroid"))
    }
    val desktopMain by getting
    desktopMain.dependencies {
      implementation(libsLibrary("kotlinx-coroutinesSwing"))
    }
  }
}