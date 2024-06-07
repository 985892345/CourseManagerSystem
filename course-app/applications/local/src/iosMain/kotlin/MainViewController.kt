
import androidx.compose.ui.window.ComposeUIViewController
import com.course.applications.local.LocalApp
import com.g985892345.provider.coursemanagersystem.courseapp.applications.local.LocalKtProviderInitializer

fun doInitApp() {
  LocalKtProviderInitializer.tryInitKtProvider()
  LocalApp.initApp()
}

fun MainViewController() = ComposeUIViewController {
  LocalApp.Content()
}
