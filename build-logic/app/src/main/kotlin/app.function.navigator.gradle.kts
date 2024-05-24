import extensions.libsBundle
import utils.Config
import utils.kspMultiplatform

plugins {
  id("app.function.serialization")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      libsBundle("navigator").forEach { implementation(it) }
    }
  }
}

ksp {
  arg("NavigatorPackageName", Config.getNamespace(project))
  arg(
    "NavigatorClassName",
    "${project.name.replaceFirstChar { it.uppercase() }}RemoteScreenEnterCollector"
  )
}

dependencies {
  val kspNavigator = rootProject.project("course-app:compiler:ksp-navigator")
  kspMultiplatform(project, kspNavigator)
}