import com.android.build.gradle.BaseExtension
import extensions.libsLibrary
import extensions.libsVersion
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
  org.jetbrains.compose
  id("app.base.multiplatform")
}

kotlin {
  sourceSets {
    val desktopMain by getting

    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material)
      implementation(compose.ui)
      @OptIn(ExperimentalComposeLibrary::class)
      implementation(compose.components.resources)
    }
    desktopMain.dependencies {
      implementation(compose.desktop.currentOs)
    }
    androidMain.dependencies {
      implementation(libsLibrary("compose-ui-tooling-preview"))
      implementation(libsLibrary("androidx-activity-compose"))
    }
  }
}

plugins.withId("com.android.base") {
  configure<BaseExtension> {
    buildFeatures.compose = true
    composeOptions {
      kotlinCompilerExtensionVersion = libsVersion("compose-compiler").requiredVersion
    }
  }
}


