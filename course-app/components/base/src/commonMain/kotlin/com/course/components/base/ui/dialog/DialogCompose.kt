package com.course.components.base.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.StateFactoryMarker
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * .
 *
 * @author 985892345
 * @date 2023/12/22 10:41
 */

fun showDialog(
  priority: Int = Int.MAX_VALUE / 2,
  properties: DialogProperties = DialogProperties(),
  onDismissRequest: Dialog.() -> Unit = { hide() },
  content: @Composable () -> Unit,
) {
  object : Dialog() {
    override val priority: Int
      get() = priority
    override val properties: DialogProperties
      get() = properties
    override val onDismissRequest: Dialog.() -> Unit
      get() = onDismissRequest

    @Composable
    override fun Content(): Unit = content.invoke()
  }.show()
}

@Stable
abstract class Dialog {
  abstract val priority: Int
  abstract val properties: DialogProperties
  abstract val onDismissRequest: Dialog.() -> Unit
  @Composable
  abstract fun Content()

  fun show() {
    if (AppDialogState === Empty) {
      AppDialogState = this
      AppDialogVisible = true
    } else {
      if (AppDialogState === this) return
      var l = 0
      var r = AppDialogList.size - 1
      while (l <= r) {
        val half = (l + r) ushr 1
        val dialog = AppDialogList[half]
        if (dialog.priority < priority) {
          l = half + 1
        } else {
          r = half - 1
        }
      }
      AppDialogList[l] = this
    }
  }

  fun hide() {
    if (AppDialogState === this) {
      AppDialogVisible = false
      return
    }
    AppDialogList.remove(this)
  }

  companion object {
    val Empty = object : Dialog() {
      override val priority: Int = Int.MAX_VALUE / 2
      override val properties: DialogProperties = DialogProperties()
      override val onDismissRequest: Dialog.() -> Unit = { hide() }
      @Composable
      override fun Content() = Unit
    }
  }
}

private val AppDialogList = mutableListOf<Dialog>()

private var AppDialogVisible by mutableStateOf(false)

private var AppDialogState: Dialog by mutableStateOf(Dialog.Empty)

@Composable
internal fun DialogCompose() {
  AnimatedVisibility(visible = AppDialogVisible, enter = fadeIn(), exit = fadeOut()) {
    Dialog(
      properties = AppDialogState.properties,
      onDismissRequest = {
        AppDialogState.onDismissRequest.invoke(AppDialogState)
      }
    ) {
      AppDialogState.Content()
    }
    DisposableEffect(Unit) {
      onDispose {
        val last = AppDialogList.removeLastOrNull()
        if (last == null) {
          AppDialogState = Dialog.Empty
        } else {
          AppDialogState = last
          AppDialogVisible = true
        }
      }
    }
  }
}