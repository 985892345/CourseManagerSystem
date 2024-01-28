import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.course.components.utils.debug.log
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 10:16
 */

fun main() = application {
  Window(onCloseRequest = ::exitApplication, title = "测试") {
    TestCompose()
  }
}

@Composable
private fun TestCompose() {
  var count by remember { mutableStateOf(0) }
  Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Button(onClick = { count++ }) {
      Text(text = "click, count = $count")
      LaunchedEffect(count) {
        log("run")
        delayUntilCancel()
      }
    }
  }
}

private suspend fun delayUntilCancel() = suspendCancellableCoroutine<Unit> {
  it.invokeOnCancellation {
    log("cancel")
  }
}