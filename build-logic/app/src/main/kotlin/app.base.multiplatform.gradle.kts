import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("kotlin-multiplatform")
}

kotlin {
  jvm("desktop")
  jvmToolchain(17)
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
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libsEx.`kotlinx-coroutines`)
      implementation(libsEx.`kotlinx-collections`)
    }
    androidMain.dependencies {
      implementation(libsEx.`kotlinx-coroutinesAndroid`)
    }
    val desktopMain by getting
    desktopMain.dependencies {
      implementation(libsEx.`kotlinx-coroutinesSwing`)
    }
  }
}