import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.course.applications.pro.ProApp
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  ProApp.initApp()
  CanvasBasedWindow(canvasElementId = "ComposeTarget") {
    TestCompose()
  }
}