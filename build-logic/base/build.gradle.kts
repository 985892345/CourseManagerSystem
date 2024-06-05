plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(libs.kotlin.gradlePlugin)
}


// 将 libs 编译在项目脚本中
// src 下面是拿不到 gradle 自动生成的 libs，这里单独生成 libsEx 去获取 libs.versions.toml 中的依赖信息
val generateLibsTask = tasks.register("generateLibs") {
  group = "build-logic"
  val versionCatalogs = project.extensions.getByType(VersionCatalogsExtension::class).named("libs")
  val libsList = versionCatalogs.libraryAliases.map { it.replace(".", "-") }
  val versionList = versionCatalogs.versionAliases.map { it.replace(".", "-") }
  val pluginsList = versionCatalogs.pluginAliases.map { it.replace(".", "-") }
  inputs.property("libsList", libsList)
  inputs.property("versionList", versionList)
  inputs.property("pluginsList", pluginsList)
  // 生成的文件在模块的 build 目录下
  val outputDir = project.layout.buildDirectory.dir(
    "generated/sources/libs/${SourceSet.MAIN_SOURCE_SET_NAME}"
  )
  outputs.dir(outputDir)
  doLast {
    val file = outputDir.get().asFile.resolve("Libs.kt")
    file.parentFile.mkdirs()
    file.delete()
    file.writeText(getLibsClass(libsList, versionList, pluginsList))
  }
}


fun getLibsClass(
  libsList: List<String>,
  versionList: List<String>,
  pluginsList: List<String>
) : String = """
// 由 build-logic/base/build.gradle.kts 生成

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugin.use.PluginDependency

val Project.libsEx: LibsEx
  get() = if (extra.has("libsEx")) extra.get("libsEx") as LibsEx else {
    LibsEx(this).also { extra.set("libsEx", it) }
  }

class LibsEx(val project: Project) {
  val versions = Versions()
  val plugins = Plugins()
  
  ${libsList.joinToString("\n  ") { 
    "val `$it`: MinimalExternalModuleDependency get() = libsLibrary(\"$it\")" 
  }}
  
  private val libs = project.extensions.getByType(VersionCatalogsExtension::class).named("libs")
  private fun libsLibrary(alias: String) = libs.findLibrary(alias).get().get()
  private fun libsBundle(alias: String) = libs.findBundle(alias).get().get()
  private fun libsVersion(alias: String) = libs.findVersion(alias).get()
  private fun libsPlugin(alias: String) = libs.findPlugin(alias).get().get()
  
  inner class Versions {
    ${versionList.joinToString("\n    ") { 
      "val `$it`: VersionConstraint get() = libsVersion(\"$it\")"
    }}
  }
  
  inner class Plugins {
    ${pluginsList.joinToString("\n    ") {
      "val `$it`: PluginDependency get() = libsPlugin(\"$it\")"
    }}
  }
}
""".trimIndent()

// 添加进编译环境和依赖环境，在编译时会自动执行 task 生成对应代码
sourceSets {
  main {
    kotlin.srcDir(generateLibsTask)
  }
}
