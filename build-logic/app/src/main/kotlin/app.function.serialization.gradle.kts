import extensions.libsLibrary
import utils.Config
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
  arg("SerializablePackageName", Config.getNamespace(project))
  arg(
    "SerializableClassName",
    "${project.name.replaceFirstChar { it.uppercase() }}ObjectSerializableCollector"
  )
}

dependencies {
  val kspSerialization = rootProject.project("course-app:compiler:ksp-serialization")
  kspMultiplatform(kspSerialization)
}
