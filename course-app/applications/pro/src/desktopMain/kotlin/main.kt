import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.Navigator
import com.course.applications.pro.TestScreen
import com.g985892345.provider.coursemanagersystem.courseapp.applications.pro.ProKtProviderInitializer

fun main() = application {
  ProKtProviderInitializer.tryInitKtProvider()
  Window(onCloseRequest = ::exitApplication, title = "CourseManagerSystem") {
    Navigator(TestScreen())
  }
}