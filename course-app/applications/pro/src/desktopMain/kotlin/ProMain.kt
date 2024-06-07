import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.launchApplication
import androidx.compose.ui.window.rememberWindowState
import com.course.applications.pro.ProApp
import com.course.components.utils.coroutine.runApp
import com.g985892345.provider.coursemanagersystem.courseapp.applications.pro.ProKtProviderInitializer

fun main() = runApp {
  ProKtProviderInitializer.tryInitKtProvider()
  ProApp.initApp()
  launchApplication {
    val width = 396
    val height = 800
    Window(
      onCloseRequest = ::exitApplication,
      title = "课表管理系统",
      state = rememberWindowState(width = width.dp, height = height.dp),
//      resizable = false,
    ) {
      remember {
        this.window.minimumSize = java.awt.Dimension(width, height)
      }
      ProApp.Content()
    }
  }
}


