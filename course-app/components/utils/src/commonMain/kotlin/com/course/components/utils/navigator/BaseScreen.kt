package com.course.components.utils.navigator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * .
 *
 * @author 985892345
 * 2024/3/25 14:23
 */
@Serializable
abstract class BaseScreen : Screen {

  @Transient
  private val windows = SnapshotStateList<Window>()

  @Composable
  final override fun Content() {
    Box(modifier = Modifier.fillMaxSize()) {
      ScreenContent()
      windows.fastForEach {
        it.content.invoke(it.dismiss)
      }
    }
  }

  @Composable
  abstract fun ScreenContent()

  fun showWindow(content: @Composable (dismiss: () -> Unit) -> Unit) {
    windows.add(Window(content))
  }

  private inner class Window(
    val content: @Composable (dismiss: () -> Unit) -> Unit,
  ) {
    val dismiss: () -> Unit = {
      windows.remove(this)
    }
  }
}