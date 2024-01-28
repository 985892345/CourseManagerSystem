import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.launchApplication
import com.course.applications.pro.ProApp
import com.course.applications.pro.ProScreenCompose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.internal.MainDispatcherFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

private lateinit var AppCoroutineScope: CoroutineScope

fun main() {
  runBlocking {
    AppCoroutineScope = this
    ProApp.initApp()
    launchApplication {
      Window(
        onCloseRequest = ::exitApplication,
        title = "课表管理系统",
        state = WindowState(width = 390.dp, height = 800.dp)
      ) {
        ProScreenCompose()
      }
    }
  }
  exitProcess(0)
}

@OptIn(InternalCoroutinesApi::class)
class DesktopMainDispatcherFactory : MainCoroutineDispatcher(), MainDispatcherFactory {
  override val loadPriority: Int
    get() = Int.MAX_VALUE

  override fun createDispatcher(allFactories: List<MainDispatcherFactory>): MainCoroutineDispatcher {
    return this
  }

  override val immediate: MainCoroutineDispatcher
    get() = this

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    AppCoroutineScope.launch {
      block.run()
    }
  }
}
