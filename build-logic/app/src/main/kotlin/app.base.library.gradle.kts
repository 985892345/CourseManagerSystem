import extensions.libsVersion
import utils.Config

plugins {
  com.android.library
  id("app.base.multiplatform")
}

android {
  namespace = Config.getNamespace(project)
  compileSdk = libsVersion("android-compileSdk").requiredVersion.toInt()
  defaultConfig {
    minSdk = libsVersion("android-minSdk").requiredVersion.toInt()
  }
}
