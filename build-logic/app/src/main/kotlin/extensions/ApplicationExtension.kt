package extensions

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension

/**
 * 抽离部分特定的配置提供给模块单独配置
 *
 * 其他配置请看 app.base.application.gradle.kts
 *
 * @author 985892345
 * @date 2024/1/12 13:36
 */
abstract class ApplicationExtension(val project: Project) {

  fun config(
    versionCode: Int,
    versionName: String,
    desktopMainClass: String,
  ) {
    project.configure<BaseAppModuleExtension> {
      defaultConfig {
        this.versionCode = versionCode
        this.versionName = versionName
      }
    }
    project.configure<ComposeExtension> {
      extensions.configure<DesktopExtension> {
        application {
          mainClass = desktopMainClass
          nativeDistributions {
            packageVersion = versionName
          }
        }
      }
    }
  }
}