import extensions.ApplicationExtension
import extensions.libsLibrary
import extensions.libsVersion
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import utils.Config

plugins {
  com.android.application
  id("app.base.multiplatform")
  id("app.function.compose")
}

/**
 * 该插件封装了大部分模版配置，其余配置由 [ApplicationExtension] 配置
 */

extensions.create("composeApplication", ApplicationExtension::class.java, project)

android {
  namespace = Config.getNamespace(project)
  compileSdk = libsVersion("android-compileSdk").requiredVersion.toInt()

  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
  sourceSets["main"].res.srcDirs("src/androidMain/res")
  sourceSets["main"].resources.srcDirs("src/commonMain/resources")

  defaultConfig {
    applicationId = Config.getNamespace(project)
    minSdk = libsVersion("android-minSdk").requiredVersion.toInt()
    targetSdk = libsVersion("android-targetSdk").requiredVersion.toInt()
  }
  buildTypes {
    release {
      isMinifyEnabled = true
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  dependencies {
    debugImplementation(libsLibrary("compose-ui-tooling"))
  }
}

compose.desktop {
  application {
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "课表管理系统"
    }
  }
}

// 用于设置 iOS 项目的 project.pbxproj
// 把模版中的 iosApp 放到模块目录下，然后运行该 task 进行修改，最后新增 iOS 配置项就可以跑起来了
// 如果启动模块依赖了新的其他模块，则需要再次运行该 task
tasks.register("setIOSProjectPbxproj") {
  group = "course"
  val file = projectDir.resolve("iosApp")
    .resolve("iosApp.xcodeproj")
    .resolve("project.pbxproj")
  val dependProjects = project.configurations
    .getByName("commonMainImplementation")
    .dependencies
    .asSequence()
    .filterIsInstance<ProjectDependency>()
    .map { it.dependencyProject }
    .toList()
  inputs.property("dependProjects", dependProjects.map { it.path })
  outputs.file(file)
  doFirst {
    val rootProjectPath = "\$SRCROOT" + project.path.split(":").joinToString("") { "/.." }
    val lines = file.readLines().toMutableList()
    val iterator = lines.listIterator()
    while (iterator.hasNext()) {
      val line = iterator.next()
      if (line.contains("shellScript = ")) {
        if (line.contains("\$SRCROOT")) {
          iterator.set(
            line.substringBeforeLast("\$SRCROOT") +
                "${rootProjectPath}\\\"\\n./gradlew ${project.path}:embedAndSignAppleFrameworkForXcode\\n\";"
          )
        }
      }
      if (line.contains("FRAMEWORK_SEARCH_PATHS")) {
        while (iterator.hasNext() && !iterator.next().contains(";")) {
          iterator.remove()
        }
        iterator.previous()
        val space = line.substringBefore("FRAMEWORK_SEARCH_PATHS") + "    "
        (dependProjects + project).map {
          space + "\"" + rootProjectPath + it.path.replace(":", "/") + "/build/xcode-frameworks/\$(CONFIGURATION)/\$(SDK_NAME)\","
        }.forEach {
          iterator.add(it)
        }
      }
      if (line.contains("OTHER_LDFLAGS")) {
        iterator.next()
        val space = iterator.next().substringBefore("\"")
        iterator.next()
        iterator.remove()
        iterator.add("${space}${Config.getBaseName(project).replaceFirstChar { it.lowercaseChar() }},")
      }
    }
    file.writeText(lines.joinToString("\n"))
  }
}