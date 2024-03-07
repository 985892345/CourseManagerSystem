import extensions.libsBundle
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
  arg("NavigatorPackageName", getPackageName(project))
  arg(
    "NavigatorClassName",
    "${project.name.replaceFirstChar { it.uppercase() }}RemoteScreenEnterCollector"
  )
}

// 添加处理轮数
ktProvider.setProcessTimes(2)

dependencies {
  val kspNavigator = rootProject.project("course-app:compiler:ksp-navigator")
  kspMultiplatform(kspNavigator)
}

fun getPackageName(project: Project): String {
  val prefix = "com.course."
  var packageName = project.name
  var p = project
  while (p.parent!!.name != "course-app") {
    p = p.parent!!
    packageName = "${p.name}.$packageName"
  }
  packageName = prefix + packageName.lowercase().replace(Regex("[^0-9a-zA-Z.]"), "")
  return packageName
}