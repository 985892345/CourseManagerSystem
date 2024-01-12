rootProject.name = "CourseManagerSystem"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  includeBuild("build-logic")
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}

plugins {
  // https://github.com/gradle/foojay-toolchains
  // https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") // mavenCentral 快照仓库
  }
}

// course-app
// applications
include("course-app:applications:pro")

// components
include("course-app:components:network")
include("course-app:components:platform")
include("course-app:components:utils")

// functions
include("course-app:functions:account")

// pages
include("course-app:pages:course")


// course-backend
include("course-backend")


// course-server
include("course-server")

// course-shared
include("course-shared:app")
include("course-shared:backend")
include("course-shared:base")
/////////////////
