package com.course.components.base.ui.dialog

import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.course.components.utils.compose.derivedStateOfStructure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

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
      if (AppDialogCoroutineScope == null) {
        AppDialogAlpha.tryEmit(1F)
        return
      }
      AppDialogCoroutineScope?.launch {
        animate(initialValue = AppDialogAlpha.value, targetValue = 1F) { value, _ ->
          AppDialogAlpha.tryEmit(value)
        }
      }
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
      if (AppDialogAlphaAnimHideJob != null) return // 说明正在隐藏
      // 此时要么正在执行显示的动画，要么显示中
      if (AppDialogList.isNotEmpty()) {
        AppDialogState = AppDialogList.removeLastOrNull()!!
      } else {
        AppDialogAlphaAnimHideJob = AppDialogCoroutineScope?.launch {
          animate(initialValue = AppDialogAlpha.value, targetValue = 0F) { value, _ ->
            AppDialogAlpha.tryEmit(value)
          }
          AppDialogAlphaAnimHideJob = null
          AppDialogState = Empty
          AppDialogList.removeLastOrNull()?.show()
        }
      }
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

private var AppDialogAlpha = MutableStateFlow(0F)

private var AppDialogAlphaAnimHideJob: Job? = null

private var AppDialogState: Dialog by mutableStateOf(Dialog.Empty)

private var AppDialogCoroutineScope: CoroutineScope? = null

@Composable
internal fun DialogCompose() {
  AppDialogCoroutineScope = rememberCoroutineScope()
  val dialogAlpha by AppDialogAlpha.collectAsState()
  if (!remember { derivedStateOfStructure { dialogAlpha == 0F } }.value) {
    Box(modifier = Modifier.fillMaxSize().graphicsLayer {
      // 官方的 AnimatedVisibility 动画要慢一帧，应该是需要重组的原因，看起来感觉就像停顿了一下，
      // 所以改为自定义 alpha 实现
      alpha = dialogAlpha
    }) {
      Dialog(
        properties = AppDialogState.properties,
        onDismissRequest = {
          AppDialogState.onDismissRequest.invoke(AppDialogState)
        }
      ) {
        Card(
          modifier = Modifier.wrapContentSize(),
          shape = RoundedCornerShape(16.dp),
          backgroundColor = Color.White,
        ) {
          AppDialogState.Content()
        }
      }
    }
  }
}