import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.Navigator
import com.course.applications.pro.ProApp
import com.course.applications.pro.TestScreen

fun main() = application {
  ProApp.initApp()
  Window(onCloseRequest = ::exitApplication, title = "CourseManagerSystem") {
    Navigator(TestScreen())
  }
}