package com.course.components.utils.compose

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.util.fastFirst
import com.course.components.utils.debug.logg
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.navigator.mainNavigator
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 在底部显示的弹窗
 *
 * @author 985892345
 * 2024/4/15 20:43
 */
fun showBottomSheetWindow(
  dismissOnBackPress: (dismiss: () -> Unit) -> Boolean = { true },
  dismissOnClickOutside: (dismiss: () -> Unit) -> Boolean = { true },
  scrimColor: Color = Color.Transparent.copy(alpha = 0.6F),
  content: @Composable BottomSheetScope.(dismiss: () -> Unit) -> Unit
) {
  var height by mutableFloatStateOf(0F)
  var offsetY by mutableFloatStateOf(0F)
  val dragValueChannel = Channel<BottomSheetDragValue?>(1, BufferOverflow.DROP_OLDEST)
  val dismiss: () -> Unit = { dragValueChannel.trySend(BottomSheetDragValue(offsetY, height)) }
  (mainNavigator.lastItem as BaseScreen).showWindow { windowDismiss ->
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Box(modifier = Modifier.fillMaxSize().focusRequester(focusRequester).focusable().onKeyEvent {
      if (it.type == KeyEventType.KeyDown && it.key == Key.Escape && dismissOnBackPress(dismiss)) {
        dismiss.invoke()
        true
      } else false
    }) {
      Spacer(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
        awaitEachGesture {
          val down = awaitFirstDown(pass = PointerEventPass.Initial)
          down.consume()
          while (true) {
            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
            val pointer = event.changes.fastFirst { it.id == down.id }
            pointer.consume()
            if (pointer.changedToUpIgnoreConsumed()) {
              if (dismissOnClickOutside(dismiss)) {
                dismiss.invoke()
              }
              break
            }
          }
        }
      }.graphicsLayer {
        alpha = if (height == 0F) 0F else (1 - offsetY / height).coerceIn(0F, 1F)
      }.background(scrimColor))
      Box(
        modifier = Modifier.align(Alignment.BottomStart)
          .fillMaxWidth()
          .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            logg("height = ${placeable.height}")
            layout(placeable.width, placeable.height) {
              if (height == 0F) {
                height = placeable.height.toFloat()
                offsetY = height
                dragValueChannel.trySend(BottomSheetDragValue(placeable.height.toFloat(), 0F))
              }
              placeable.place(0, offsetY.roundToInt())
            }
          }
      ) {
        val scope = object : BottomSheetScope {
          @Composable
          override fun Modifier.bottomSheetDraggable(): Modifier {
            return draggable(
              orientation = Orientation.Vertical,
              state = rememberDraggableState {
                val newOffset = offsetY + it
                offsetY = if (newOffset < 0) 0F else newOffset
              },
              onDragStarted = {
                dragValueChannel.trySend(null)
              },
              onDragStopped = { velocity ->
                if (velocity > 1000) {
                  dragValueChannel.trySend(BottomSheetDragValue(offsetY, height, velocity))
                } else if (velocity < -1000) {
                  dragValueChannel.trySend(BottomSheetDragValue(offsetY, 0F, velocity))
                } else if (offsetY > height / 2) {
                  dragValueChannel.trySend(BottomSheetDragValue(offsetY, height, velocity))
                } else {
                  dragValueChannel.trySend(BottomSheetDragValue(offsetY, 0F, velocity))
                }
              }
            )
          }
        }
        content(scope, dismiss)
      }
      LaunchedEffect(Unit) {
        var prevVelocity = 0F
        var job: Job? = null
        while (true) {
          val dragValue = dragValueChannel.receive()
          job?.cancel()
          job = null
          if (dragValue != null) {
            job = launch {
              animate(
                initialValue = dragValue.initialValue,
                targetValue = dragValue.targetValue,
                initialVelocity = dragValue.initialVelocity(prevVelocity),
              ) { value, velocity ->
                prevVelocity = velocity
                offsetY = value.coerceAtLeast(0F)
                logg("offsetY = $offsetY")
              }
              if (dragValue.targetValue == height) {
                windowDismiss()
              }
            }
          }
        }
      }
    }
  }
}

interface BottomSheetScope {
  @Composable
  fun Modifier.bottomSheetDraggable(): Modifier
}

private class BottomSheetDragValue(
  val initialValue: Float,
  val targetValue: Float,
  private val initialVelocity: Float = Float.POSITIVE_INFINITY,
) {
  fun initialVelocity(default: Float): Float =
    if (initialVelocity == Float.POSITIVE_INFINITY) default else initialVelocity
}


