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
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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

  @Transient
  internal val backHandleList = mutableListOf<BackHandle>()

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

  fun showWindow(
    dismissOnBackPress: (dismiss: () -> Unit) -> Boolean = { true },
    content: @Composable (dismiss: () -> Unit) -> Unit
  ) {
    windows.add(Window(dismissOnBackPress, content))
  }

  // 限制只允许在类全局变量注册
  fun registerBackHandle(
    enabled: Boolean = true,
    onBackPressed: () -> Unit
  ): ReadWriteProperty<BaseScreen, Boolean> {
    val backHandle = BackHandle(enabled, onBackPressed)
    backHandleList.add(backHandle)
    return object : ReadWriteProperty<BaseScreen, Boolean> {
      override fun getValue(thisRef: BaseScreen, property: KProperty<*>): Boolean {
        return backHandle.enabled
      }

      override fun setValue(thisRef: BaseScreen, property: KProperty<*>, value: Boolean) {
        backHandle.enabled = value
      }
    }
  }

  private inner class Window(
    val dismissOnBackPress: (dismiss: () -> Unit) -> Boolean,
    val content: @Composable (dismiss: () -> Unit) -> Unit,
  ) {

    private val backHandle = BackHandle {
      if (dismissOnBackPress(dismiss)) {
        if (windows.contains(this)) {
          dismiss.invoke()
        }
      }
    }

    val dismiss: () -> Unit = {
      windows.remove(this)
      backHandleList.remove(backHandle)
    }

    init {
      backHandleList.add(backHandle)
    }
  }

  internal class BackHandle(
    var enabled: Boolean = true,
    val onBackPressed: () -> Unit,
  )
}