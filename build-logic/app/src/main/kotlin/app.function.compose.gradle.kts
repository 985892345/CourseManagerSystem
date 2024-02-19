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

// 输出 compose 稳定性报告，执行 outputCompilerReports 任务
// https://developer.android.com/jetpack/compose/performance/stability/diagnose#compose-compiler
tasks.register("outputCompilerReports") {
  group = "compose"
  doLast {
    exec {
      commandLine(
        rootProject.projectDir.resolve(
          "gradlew" +
          if (System.getProperty("os.name").contains("windows")) ".bat" else ""
        ).absolutePath,
        "${project.path}:assembleDebug",
        "-PcomposeCompilerReports=true"
      )
    }
  }
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions {
    if (project.findProperty("composeCompilerReports") == "true") {
      freeCompilerArgs += listOf(
        "-P",
        "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
            "${layout.buildDirectory.get().asFile.absolutePath}/compose_compiler"
      )
    }
    if (project.findProperty("composeCompilerMetrics") == "true") {
      freeCompilerArgs += listOf(
        "-P",
        "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
            "${layout.buildDirectory.get().asFile.absolutePath}/compose_compiler"
      )
    }
  }
}
