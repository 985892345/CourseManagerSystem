import extensions.libsVersion
import utils.Config

plugins {
  id("com.android.library")
  id("app.base.multiplatform")
}

android {
  namespace = Config.getNamespace(project)
  compileSdk = libsVersion("android-compileSdk").requiredVersion.toInt()

  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
  sourceSets["main"].res.srcDirs("src/androidMain/res")
  sourceSets["main"].resources.srcDirs("src/commonMain/resources")

  defaultConfig {
    minSdk = libsVersion("android-minSdk").requiredVersion.toInt()
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}
