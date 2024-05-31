import com.android.build.gradle.BaseExtension

plugins {
  id("org.jetbrains.kotlin.plugin.compose")
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
      implementation(libsEx.`compose-ui-tooling-preview`)
      implementation(libsEx.`androidx-activity-compose`)
    }
  }
}

plugins.withId("com.android.base") {
  configure<BaseExtension> {
    buildFeatures.compose = true
    @Suppress("UnstableApiUsage")
    composeOptions {
      kotlinCompilerExtensionVersion = libsEx.versions.`compose-compiler`.requiredVersion
    }
  }
}

composeCompiler {
  // 输出 compose 稳定性报告，执行 outputCompilerReports 任务
  // https://developer.android.com/jetpack/compose/performance/stability/diagnose#compose-compiler
  reportsDestination.set(
    layout.buildDirectory.get().asFile.resolve("compose_compiler")
  )

  // 对 Compose 配置外部类的稳定性
  // 只允许配置已有第三方库里面的类，如果是自己的类请打上 @Stable 注解
  // 配置规则可以查看 https://android-review.googlesource.com/c/platform/frameworks/support/+/2668595
  stabilityConfigurationFile.set(
    rootDir.resolve("config").resolve("compose-stability-config.txt")
  )
}
