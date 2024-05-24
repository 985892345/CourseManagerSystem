import utils.Config

plugins {
  id("kotlin-multiplatform")
}

kotlin {
  jvm()
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
//  @Suppress("OPT_IN_USAGE")
//  wasmJs {
//    browser()
//  }
}