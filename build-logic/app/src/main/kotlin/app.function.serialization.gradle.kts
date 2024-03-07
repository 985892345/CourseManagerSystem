import extensions.libsLibrary
import utils.kspMultiplatform

plugins {
  id("app.function.provider")
  id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libsLibrary("kotlinx-serialization"))
    }
  }
}

ksp {
  arg("SerializablePackageName", getPackageName(project))
  arg(
    "SerializableClassName",
    "${project.name.replaceFirstChar { it.uppercase() }}ObjectSerializableCollector"
  )
}

// 添加处理轮数
ktProvider.setProcessTimes(2)

dependencies {
  val kspSerialization = rootProject.project("course-app:compiler:ksp-serialization")
  kspMultiplatform(kspSerialization)
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