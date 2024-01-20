import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.g985892345.provider.coursemanagersystem.courseapp.applications.pro.ProKtProviderInitializer
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  ProKtProviderInitializer.tryInitKtProvider()
  CanvasBasedWindow(canvasElementId = "ComposeTarget") {
    TestCompose()
  }
}