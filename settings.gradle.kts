rootProject.name = "CourseManagerSystem"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  includeBuild("build-logic")
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") // mavenCentral 快照仓库
  }
}

plugins {
  // https://github.com/gradle/foojay-toolchains
  // https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

dependencyResolutionManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") // mavenCentral 快照仓库
  }
}


/////////////  自动 include 模块  ///////////

// 需要删除模块时写这里面，将不再进行 include，直接写模块名即可
val excludeList: List<String> = listOf(
  "app-local"
)

fun includeModule(topName: String, file: File) {
  if (!file.resolve("settings.gradle.kts").exists()) {
    if (file.resolve("build.gradle.kts").exists()) {
      var path = ""
      var nowFile = file
      while (nowFile.name != topName) {
        path = ":${nowFile.name}$path"
        nowFile = nowFile.parentFile
      }
      path = "${topName}$path"
      include(path)
    }
  }
  // 递归寻找所有子模块
  file.listFiles()?.filter {
    it.name != "src" // 去掉 src 文件夹
        && it.name != "build"
        && it.name != "iosApp"
        && !it.resolve("settings.gradle.kts").exists() // 去掉独立的项目模块，比如 build-logic
        && !excludeList.contains(it.name) // 去掉被忽略的模块
  }?.forEach {
    includeModule(topName, it)
  }
}

includeModule("course-app", rootDir.resolve("course-app"))
includeModule("course-server", rootDir.resolve("course-server"))
includeModule("course-shared", rootDir.resolve("course-shared"))
includeModule("course-source", rootDir.resolve("course-source"))
/**
 * 如果你使用 AS 自带的模块模版，他会自动添加 include()，请删除掉，因为上面会自动读取
 */

include(":composeApp")