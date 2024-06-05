import utils.kspMultiplatform

plugins {
  id("app.function.serialization")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libsEx.`voyager-navigator`)
      implementation(libsEx.`voyager-screenmodel`)
      implementation(libsEx.`voyager-bottomSheetNavigator`)
      implementation(libsEx.`voyager-transitions`)
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