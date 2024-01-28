package com.course.components.base.ui.dialog

import androidx.compose.runtime.Composable
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

@StateFactoryMarker
fun showDialog(
  properties: DialogProperties = DialogProperties(),
  onDismissRequest: Dialog.() -> Unit = { hide() },
  content: @Composable () -> Unit,
) = object : Dialog() {
  override val properties: DialogProperties
    get() = properties
  override val onDismissRequest: Dialog.() -> Unit
    get() = onDismissRequest

  @Composable
  override fun Content(): Unit = content.invoke()
}

@Stable
abstract class Dialog {
  abstract val properties: DialogProperties
  abstract val onDismissRequest: Dialog.() -> Unit
  @Composable
  abstract fun Content()

  fun show(): Boolean {
    if (!AppDialogEnable) {
      AppDialogState = this
      AppDialogEnable = true
      return true
    }
    return false
  }

  fun hide() {
    if (AppDialogEnable) {
      AppDialogEnable = false
    }
  }

  companion object : Dialog() {
    override val properties: DialogProperties = DialogProperties()
    override val onDismissRequest: Dialog.() -> Unit = { hide() }
    @Composable
    override fun Content() = Unit
  }
}

private var AppDialogEnable by mutableStateOf(false)

private var AppDialogState: Dialog by mutableStateOf(Dialog)

@Composable
internal fun DialogCompose() {
  if (AppDialogEnable) {
    Dialog(
      properties = AppDialogState.properties,
      onDismissRequest = {
        AppDialogState.onDismissRequest.invoke(AppDialogState)
      }
    ) {
      AppDialogState.Content()
    }
  }
}