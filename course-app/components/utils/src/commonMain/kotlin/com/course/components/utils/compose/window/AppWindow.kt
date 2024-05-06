package com.course.components.utils.compose.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.util.fastForEach

/**
 * 轻量级的窗口，会在页面上方进行显示，类似于 Dialog 但不会阻塞页面的交互
 *
 * @author 985892345
 * 2024/4/26 19:37
 */
class AppWindow(
  val content: @Composable (AppWindow) -> Unit
) {
  fun show() {
    AppWindowList.add(this)
  }

  fun dismiss() {
    AppWindowList.remove(this)
  }
}

private val AppWindowList = SnapshotStateList<AppWindow>()

@Composable
fun AppWindowCompose() {
  AppWindowList.fastForEach {
    it.content.invoke(it)
  }
}