import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
  id("app.base.application")
  id("app.function.provider")
  id("app.function.serialization")
  id("app.function.navigator")
}

composeApplication {
  config(
    versionCode = 1,
    versionName = "1.0.0",
    desktopMainClass = "MainKt",
  )
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      val courseAppFile = rootDir.resolve("course-app")
      implementationModules("components", courseAppFile.resolve("components"))
      implementationModules("functions", courseAppFile.resolve("functions"))
      implementationModules("pages", courseAppFile.resolve("pages"))
    }
  }
}

fun KotlinDependencyHandler.implementationModules(topName: String, file: File) {
  if (!file.resolve("settings.gradle.kts").exists()) {
    if (file.resolve("build.gradle.kts").exists()) {
      var path = ""
      var nowFile = file
      while (nowFile.name != topName) {
        path = ":${nowFile.name}$path"
        nowFile = nowFile.parentFile
      }
      path = "course-app:${topName}$path"
      rootProject.findProject(path)?.let { implementation(it) }
    }
  }
  // 递归寻找所有子模块
  file.listFiles()?.filter {
    it.name != "src" // 去掉 src 文件夹
        && it.name != "build"
        && it.name != "iosApp"
        && !it.resolve("settings.gradle.kts").exists() // 去掉独立的项目模块，比如 build-logic
  }?.forEach {
    implementationModules(topName, it)
  }
}