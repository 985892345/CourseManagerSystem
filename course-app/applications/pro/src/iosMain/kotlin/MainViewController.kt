
import androidx.compose.ui.window.ComposeUIViewController
import com.course.components.base.page.MainPageCompose
import com.course.pages.main.MainScreen

fun MainViewController() = ComposeUIViewController {
  MainPageCompose(MainScreen)
}
