import com.android.build.gradle.BaseExtension
import extensions.libsLibrary
import extensions.libsVersion

plugins {
  id("org.jetbrains.compose")
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
      implementation(compose.components.resources)
      implementation(compose.components.uiToolingPreview)
      implementation(compose.materialIconsExtended)
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

// 对 Compose 配置外部类的稳定性
// 只允许配置已有第三方库里面的类，如果是自己的类请打上 @Stable 注解
// 配置规则可以查看 https://android-review.googlesource.com/c/platform/frameworks/support/+/2668595
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
//  kotlinOptions {
//    freeCompilerArgs += listOf(
//      "-P",
//      "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=" +
//          "$rootDir/config/compose-stability-config.txt"
//    )
//  }
//}
// 草，Compose Multiplatform 没有加这个配置