import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.course.applications.pro.ProScreenCompose
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  ProApp.initApp()
  CanvasBasedWindow(canvasElementId = "ComposeTarget") {
    ProScreenCompose()
  }
}