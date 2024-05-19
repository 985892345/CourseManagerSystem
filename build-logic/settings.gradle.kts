pluginManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}

dependencyResolutionManagement {
  repositories {
    mavenLocal() // maven 默认的本地依赖位置：用户名/.m2/repository 中
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") // mavenCentral 快照仓库
  }
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}
rootProject.name = "build-logic"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("base")
include("shared")
include("app")
include("backend")