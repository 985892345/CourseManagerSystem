import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.launchApplication
import com.course.applications.pro.ProApp
import com.course.applications.pro.ProMainScreen
import com.course.components.base.page.MainPageCompose
import com.course.components.utils.coroutine.runApp

fun main() = runApp {
  ProApp.initApp()
  launchApplication {
    Window(
      onCloseRequest = ::exitApplication,
      title = "课表管理系统",
      state = WindowState(width = 390.dp, height = 800.dp),
//      resizable = false,
    ) {
      MainPageCompose(remember { ProMainScreen() })
    }
  }
}



