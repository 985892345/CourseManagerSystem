import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.course.applications.pro.ProMainScreen
import com.course.components.base.page.MainPageCompose

fun MainViewController() = ComposeUIViewController {
  MainPageCompose(remember { ProMainScreen() })
}
