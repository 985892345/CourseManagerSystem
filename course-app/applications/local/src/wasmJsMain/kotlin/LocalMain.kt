import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.course.applications.pro.ProScreenCompose
import com.g985892345.provider.coursemanagersystem.courseapp.applications.local.LocalKtProviderInitializer

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  LocalKtProviderInitializer.tryInitKtProvider()
  ProApp.initApp()
  CanvasBasedWindow(canvasElementId = "ComposeTarget") {
    ProScreenCompose()
  }
}