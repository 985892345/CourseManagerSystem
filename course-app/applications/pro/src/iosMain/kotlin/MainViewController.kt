
import androidx.compose.ui.window.ComposeUIViewController
import com.course.applications.pro.ProMainRemoteScreen
import com.course.components.base.page.MainPageCompose

fun MainViewController() = ComposeUIViewController {
  MainPageCompose(ProMainRemoteScreen)
}
