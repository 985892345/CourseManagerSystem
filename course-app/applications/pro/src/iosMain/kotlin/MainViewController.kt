
import androidx.compose.ui.window.ComposeUIViewController
import com.course.applications.pro.ProApp
import com.g985892345.provider.coursemanagersystem.courseapp.applications.pro.ProKtProviderInitializer

fun initApp() {
  ProKtProviderInitializer.tryInitKtProvider()
  ProApp.initApp()
}

fun MainViewController() = ComposeUIViewController {
  ProApp.Content()
}
